import { defineConfig, globalIgnores } from 'eslint/config'
import globals from 'globals'
import js from '@eslint/js'
import tseslintParser from '@typescript-eslint/parser'
import vueParser from 'vue-eslint-parser'
import pluginVue from 'eslint-plugin-vue'
import pluginOxlint from 'eslint-plugin-oxlint'
import skipFormatting from 'eslint-config-prettier/flat'

export default defineConfig([
  globalIgnores([
    '**/dist/**',
    '**/dist-ssr/**',
    '**/coverage/**',
    // unplugin 自动生成的类型声明
    'src/auto-imports.d.ts',
    'src/components.d.ts',
  ]),

  {
    name: 'app/files-to-lint',
    files: ['**/*.{vue,js,mjs,ts}'],
    languageOptions: {
      ecmaVersion: 'latest',
      sourceType: 'module',
      globals: {
        ...globals.browser,
        // unplugin-auto-import 自动注入,已由 auto-imports.d.ts 声明
        ElMessage: 'readonly',
        ElMessageBox: 'readonly',
      },
    },
  },

  // .ts 文件:交给 @typescript-eslint/parser
  {
    files: ['**/*.ts'],
    languageOptions: {
      parser: tseslintParser,
    },
  },

  // .vue 文件:用 vue-eslint-parser 解析模板,内部脚本交给 TS parser
  {
    files: ['**/*.vue'],
    languageOptions: {
      parser: vueParser,
      parserOptions: {
        parser: tseslintParser,
      },
    },
  },

  // vite.config.js 运行于 Node 环境
  {
    files: ['vite.config.js'],
    languageOptions: {
      globals: {
        ...globals.node,
      },
    },
  },

  js.configs.recommended,
  ...pluginVue.configs['flat/essential'],

  {
    rules: {
      'vue/multi-word-component-names': 'off',
      'no-unused-vars': 'off',
    },
  },

  ...pluginOxlint.buildFromOxlintConfigFile('.oxlintrc.json'),

  skipFormatting,
])
