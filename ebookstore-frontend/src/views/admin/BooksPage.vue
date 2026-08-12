<template>
  <div class="admin-books">
    <div class="page-header">
      <h2 class="page-title">图书管理</h2>
      <div class="header-actions">
        <el-button type="primary" @click="showEditDialog = true; editingBook = null">
          <el-icon><Plus /></el-icon> 新增图书
        </el-button>
      </div>
    </div>

    <div class="filter-bar">
      <el-select v-model="filterCategory" placeholder="选择分类" clearable @change="loadBooks">
        <el-option
          v-for="cat in categories"
          :key="cat.id"
          :label="cat.name"
          :value="cat.id"
        />
      </el-select>

      <el-input
        v-model="searchKeyword"
        placeholder="搜索书名或作者"
        prefix-icon="Search"
        style="width: 300px;"
        clearable
        @clear="loadBooks"
        @keyup.enter="loadBooks"
      />

      <el-button type="primary" @click="loadBooks">搜索</el-button>
    </div>

    <el-table :data="books" style="width: 100%" v-loading="loading">
      <el-table-column type="index" label="#" width="50" />
      <el-table-column prop="title" label="书名" min-width="200" />
      <el-table-column prop="author" label="作者" width="120" />
      <el-table-column label="售价" width="100">
        <template #default="{ row }">
          ¥{{ row.price }}
        </template>
      </el-table-column>
      <el-table-column label="成本" width="100">
        <template #default="{ row }">
          ¥{{ row.costPrice || '-' }}
        </template>
      </el-table-column>
      <el-table-column prop="stock" label="库存" width="80" />
      <el-table-column prop="salesCount" label="销量" width="80" />
      <el-table-column label="状态" width="80">
        <template #default="{ row }">
          <el-tag :type="row.status === 1 ? 'success' : 'info'" size="small">
            {{ row.status === 1 ? '上架' : '下架' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="160" fixed="right">
        <template #default="{ row }">
          <el-button link type="primary" @click="editBook(row)">编辑</el-button>
          <el-button link :type="row.status === 1 ? 'warning' : 'success'" @click="toggleStatus(row)">
            {{ row.status === 1 ? '下架' : '上架' }}
          </el-button>
        </template>
      </el-table-column>
    </el-table>

    <el-dialog v-model="showEditDialog" :title="editingBook ? '编辑图书' : '新增图书'" width="700px">
      <el-form :model="bookForm" label-width="100px">
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="书名">
              <el-input v-model="bookForm.title" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="作者">
              <el-input v-model="bookForm.author" />
            </el-form-item>
          </el-col>
        </el-row>

        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="ISBN">
              <el-input v-model="bookForm.isbn" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="出版社">
              <el-input v-model="bookForm.publisher" />
            </el-form-item>
          </el-col>
        </el-row>

        <el-row :gutter="20">
          <el-col :span="8">
            <el-form-item label="售价">
              <el-input-number v-model="bookForm.price" :min="0" :precision="2" controls-position="right" />
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="成本">
              <el-input-number v-model="bookForm.costPrice" :min="0" :precision="2" controls-position="right" />
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="库存">
              <el-input-number v-model="bookForm.stock" :min="0" controls-position="right" />
            </el-form-item>
          </el-col>
        </el-row>

        <el-form-item label="分类">
          <el-select v-model="bookForm.categoryId" placeholder="请选择分类">
            <el-option
              v-for="cat in allCategories"
              :key="cat.id"
              :label="cat.name"
              :value="cat.id"
            />
          </el-select>
        </el-form-item>

        <el-form-item label="难度">
          <el-radio-group v-model="bookForm.difficultyLevel">
            <el-radio :value="1">入门</el-radio>
            <el-radio :value="2">进阶</el-radio>
            <el-radio :value="3">高级</el-radio>
          </el-radio-group>
        </el-form-item>

        <el-form-item label="简介">
          <el-input v-model="bookForm.description" type="textarea" :rows="3" />
        </el-form-item>

        <el-form-item label="封面URL">
          <el-input v-model="bookForm.coverImage" placeholder="/images/xxx.jpg" />
        </el-form-item>
      </el-form>

      <template #footer>
        <el-button @click="showEditDialog = false">取消</el-button>
        <el-button type="primary" :loading="saving" @click="saveBook">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { getLevel1Categories, getLevel2Categories, getBookList } from '@/api/book'
import { createBook, updateBook, toggleBookStatus } from '@/api/admin'

const loading = ref(false)
const saving = ref(false)
const books = ref([])
const categories = ref([])
const allCategories = ref([])
const filterCategory = ref(null)
const searchKeyword = ref('')
const showEditDialog = ref(false)
const editingBook = ref(null)
const bookForm = ref(createEmptyForm())

function createEmptyForm() {
  return {
    title: '',
    author: '',
    isbn: '',
    publisher: '',
    price: 0,
    costPrice: 0,
    stock: 0,
    categoryId: null,
    difficultyLevel: 1,
    description: '',
    coverImage: ''
  }
}

onMounted(async () => {
  await loadCategories()
  await loadBooks()
})

const loadCategories = async () => {
  try {
    const res = await getLevel1Categories()
    const cats = res.data || res || []
    categories.value = cats

    for (const cat of cats) {
      try {
        const subRes = await getLevel2Categories(cat.id)
        const subs = subRes.data || subRes || []
        allCategories.value.push(...subs)
      } catch (e) {
        console.error(e)
      }
    }
  } catch (error) {
    console.error(error)
  }
}

const loadBooks = async () => {
  loading.value = true
  try {
    let result = []

    if (filterCategory.value) {
      const res = await getBookList(filterCategory.value)
      result = res.data || res || []
    } else {
      // ✅ 修复：遍历所有二级分类加载图书（不再使用不存在的方法）
      for (const cat of allCategories.value) {
        try {
          const res = await getBookList(cat.id)
          const catBooks = res.data || res || []
          result = result.concat(catBooks)
        } catch (e) {
          console.error(e)
        }
      }
    }

    if (searchKeyword.value) {
      const keyword = searchKeyword.value.toLowerCase()
      result = result.filter(
        b => b.title?.toLowerCase().includes(keyword) ||
             b.author?.toLowerCase().includes(keyword)
      )
    }

    books.value = result
  } catch (error) {
    console.error(error)
  } finally {
    loading.value = false
  }
}

const editBook = (row) => {
  editingBook.value = row
  bookForm.value = {
    title: row.title || '',
    author: row.author || '',
    isbn: row.isbn || '',
    publisher: row.publisher || '',
    price: row.price || 0,
    costPrice: row.costPrice || 0,
    stock: row.stock || 0,
    categoryId: row.categoryId || null,
    difficultyLevel: row.difficultyLevel || 1,
    description: row.description || '',
    coverImage: row.coverImage || ''
  }
  showEditDialog.value = true
}

const toggleStatus = async (row) => {
  const action = row.status === 1 ? '下架' : '上架'
  try {
    await ElMessageBox.confirm(`确定要${action}该图书吗？`, '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    await toggleBookStatus(row.id)
    ElMessage.success(`${action}成功`)
    loadBooks()
  } catch (error) {
    if (error !== 'cancel') console.error(error)
  }
}

const saveBook = async () => {
  saving.value = true
  try {
    if (editingBook.value) {
      await updateBook(editingBook.value.id, bookForm.value)
    } else {
      await createBook(bookForm.value)
    }
    ElMessage.success(editingBook.value ? '图书信息已更新' : '图书已添加')
    showEditDialog.value = false
    bookForm.value = createEmptyForm()
    editingBook.value = null
    loadBooks()
  } catch (error) {
    console.error(error)
  } finally {
    saving.value = false
  }
}
</script>

<style scoped>
.admin-books {
  padding: 20px;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.page-title {
  font-size: 24px;
  color: #333;
}

.filter-bar {
  display: flex;
  gap: 15px;
  margin-bottom: 20px;
  align-items: center;
}
</style>
