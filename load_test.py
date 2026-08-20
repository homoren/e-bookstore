#!/usr/bin/env python3
"""
Ebookstore 压测脚本(量要大?并发拉满!)

用法:
  python load_test.py --url http://localhost:8080                 # 本地,默认并发50/500请求
  python load_test.py --url http://localhost:8080 --concurrency 200 --count 5000
  python load_test.py --url http://你的服务器IP:8080 --concurrency 500 --count 10000 --login

参数:
  --url           后端地址,默认 http://localhost:8080
  --concurrency   并发数,默认 50
  --count         总请求数,默认 500
  --login         是否包含登录接口(会先注册临时账号,测 BCrypt 压力)
"""
import argparse
import concurrent.futures as cf
import random
import statistics
import time
from collections import Counter

import requests


def build_targets(base_url, with_login=False, token=None):
    """贴合真实用户行为的接口组合(图书详情/分页更热门,权重更高)"""
    targets = [
        {"name": "分类列表", "method": "GET", "url": f"{base_url}/api/books/categories/level1", "weight": 1},
        {"name": "公告列表", "method": "GET", "url": f"{base_url}/api/announcements/list", "weight": 1},
        {"name": "图书列表", "method": "GET", "url": f"{base_url}/api/books/list", "params": {"categoryId": "0"}, "weight": 1},
        {"name": "图书分页", "method": "GET", "url": f"{base_url}/api/books/page",
         "params": {"page": "1", "pageSize": "12", "sort": "default"}, "weight": 2},
        {"name": "图书详情", "method": "GET", "url": f"{base_url}/api/books/detail/24", "weight": 3},
    ]
    if with_login:
        targets.append({"name": "登录(BCrypt)", "method": "POST", "url": f"{base_url}/api/users/login",
                        "body": {"username": "loadtest_user", "password": "123456"}, "weight": 1})
    return targets


def do_one(target, token=None):
    headers = {}
    if token:
        headers["Authorization"] = f"Bearer {token}"
    start = time.time()
    try:
        if target["method"] == "POST":
            resp = requests.post(target["url"], json=target.get("body"), headers=headers, timeout=10)
        else:
            resp = requests.get(target["url"], params=target.get("params"), headers=headers, timeout=10)
        status = resp.status_code
        ok = status < 500
    except requests.RequestException:
        status = 0
        ok = False
    latency = (time.time() - start) * 1000
    return {"ok": ok, "status": status, "latency": latency, "name": target["name"]}


def ensure_login(base_url):
    """注册+登录一个临时账号,返回 token;失败返回 None(登录接口不可用则跳过)"""
    try:
        requests.post(f"{base_url}/api/users/register", json={
            "username": "loadtest_user", "password": "123456",
            "realName": "压测", "phone": "13800138000", "address": "测试"})
        resp = requests.post(f"{base_url}/api/users/login", json={
            "username": "loadtest_user", "password": "123456"}, timeout=10)
        if resp.status_code == 200 and resp.json().get("data", {}).get("token"):
            return resp.json()["data"]["token"]
    except Exception:
        pass
    return None


def run(base_url, concurrency, count, with_login):
    token = ensure_login(base_url) if with_login else None
    if with_login:
        print(f"{'登录可用,token 已获取' if token else '登录接口不可用,跳过登录接口'}")

    targets = build_targets(base_url, with_login, token)

    print(f"预热 {len(targets)} 个接口...")
    for t in targets:
        do_one(t, token)
    time.sleep(0.5)

    pool = []
    for t in targets:
        pool.extend([t] * t["weight"])

    print(f"开始压测: 并发={concurrency}, 总请求={count}(Ctrl+C 可随时中断,显示已完成部分)\n")
    wall_start = time.time()

    results = []
    interrupted = False
    try:
        with cf.ThreadPoolExecutor(max_workers=concurrency) as ex:
            futures = [ex.submit(do_one, random.choice(pool), token) for _ in range(count)]
            for i, f in enumerate(cf.as_completed(futures), 1):
                results.append(f.result())
                # 每 10% 打印一次进度
                if i % max(1, count // 10) == 0:
                    print(f"  进度: {i}/{count} ({i/count*100:.0f}%)", flush=True)
    except KeyboardInterrupt:
        interrupted = True
        print("\n收到 Ctrl+C,正在汇总已完成的部分...")

    wall = time.time() - wall_start
    total = len(results)
    if interrupted and total < count:
        print(f"(已中断,仅统计完成的 {total} 个请求)")
    ok_count = sum(1 for r in results if r["ok"])
    latencies = sorted(r["latency"] for r in results)

    print("=" * 56)
    print(f"总请求        : {total}")
    print(f"成功/失败     : {ok_count} / {total - ok_count}")
    print(f"成功率        : {ok_count / total * 100:.2f}%")
    print(f"总耗时        : {wall:.2f} s")
    print(f"QPS           : {total / wall:.0f}")
    if latencies:
        print(f"平均延迟      : {statistics.mean(latencies):.1f} ms")
        print(f"P50 / P95 / P99: {latencies[int(total*0.50)]:.1f} / {latencies[int(total*0.95)]:.1f} / {latencies[int(total*0.99)]:.1f} ms")
    print(f"状态码分布    : {dict(sorted(Counter(r['status'] for r in results).items()))}")
    print("=" * 56)

    # 按接口维度汇总(看缓存接口 vs 未缓存接口的差异)
    print("\n按接口明细(平均延迟 / 成功率):")
    by_name = {}
    for r in results:
        by_name.setdefault(r["name"], []).append(r)
    for name, rs in sorted(by_name.items()):
        oks = sum(1 for r in rs if r["ok"])
        avg = statistics.mean(r["latency"] for r in rs)
        print(f"  {name:<16} 请求{len(rs):>5}  成功率{oks/len(rs)*100:>6.1f}%  平均{avg:>8.1f}ms")


if __name__ == "__main__":
    ap = argparse.ArgumentParser(description="Ebookstore 压测脚本")
    ap.add_argument("--url", default="http://localhost:8080", help="后端地址")
    ap.add_argument("--concurrency", type=int, default=50, help="并发数(默认50)")
    ap.add_argument("--count", type=int, default=500, help="总请求数(默认500)")
    ap.add_argument("--login", action="store_true", help="是否包含登录接口(BCrypt 压力)")
    args = ap.parse_args()

    run(args.url, args.concurrency, args.count, args.login)
