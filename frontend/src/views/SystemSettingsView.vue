<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { Plus, Edit2, Trash2, Tag, X, Check, Building2, Settings2 } from 'lucide-vue-next'
import { useCategoryStore } from '../stores/categoryStore'
import { useDepartmentStore } from '../stores/departmentStore'

const categoryStore = useCategoryStore()
const departmentStore = useDepartmentStore()

const activeTab = ref<'categories' | 'departments'>('categories')

// Category local state
const newCategoryName = ref('')
const newCategoryUseOcr = ref(false)
const editingCategoryId = ref<number | null>(null)
const editingCategoryName = ref('')
const editingCategoryUseOcr = ref(false)

// Department local state
const newDepartmentName = ref('')
const editingDepartmentId = ref<number | null>(null)
const editingDepartmentName = ref('')

onMounted(async () => {
  categoryStore.fetchCategories()
  departmentStore.fetchDepartments()
})

// Category Handlers
const handleAddCategory = async () => {
  if (!newCategoryName.value.trim()) return
  try {
    await categoryStore.addCategory(newCategoryName.value, newCategoryUseOcr.value)
    newCategoryName.value = ''
    newCategoryUseOcr.value = false
  } catch (err: any) {
    alert(err.response?.data?.message || err.response?.data || 'Failed to add category')
  }
}

const startEditCategory = (category: any) => {
  editingCategoryId.value = category.id
  editingCategoryName.value = category.name
  editingCategoryUseOcr.value = category.useOcr
}

const handleUpdateCategory = async (id: number) => {
  if (!editingCategoryName.value.trim()) return
  try {
    await categoryStore.updateCategory(id, editingCategoryName.value, editingCategoryUseOcr.value)
    editingCategoryId.value = null
  } catch (err: any) {
    alert(err.response?.data?.message || err.message || 'Failed to update category')
  }
}

const handleDeleteCategory = async (id: number) => {
  if (confirm('카테고리를 삭제하시겠습니까? 연결된 자산이 있을 수 있습니다.')) {
    try {
      await categoryStore.deleteCategory(id)
    } catch (err: any) {
      alert(err.response?.data?.message || err.message || 'Failed to delete category')
    }
  }
}

// Department Handlers
const handleAddDepartment = async () => {
  if (!newDepartmentName.value.trim()) return
  try {
    await departmentStore.addDepartment(newDepartmentName.value)
    newDepartmentName.value = ''
  } catch (err: any) {
    alert(err.response?.data?.message || err.response?.data || 'Failed to add department')
  }
}

const startEditDepartment = (dept: any) => {
  editingDepartmentId.value = dept.id
  editingDepartmentName.value = dept.name
}

const handleUpdateDepartment = async (id: number) => {
  if (!editingDepartmentName.value.trim()) return
  try {
    await departmentStore.updateDepartment(id, editingDepartmentName.value)
    editingDepartmentId.value = null
  } catch (err: any) {
    alert(err.response?.data?.message || err.message || 'Failed to update department')
  }
}

const handleDeleteDepartment = async (id: number) => {
  if (confirm('부서를 삭제하시겠습니까? 해당 부서 소속 사용자가 있을 수 있습니다.')) {
    try {
      await departmentStore.deleteDepartment(id)
    } catch (err: any) {
      alert(err.response?.data?.message || err.message || 'Failed to delete department')
    }
  }
}
</script>

<template>
  <div class="max-w-6xl mx-auto py-8 px-4">
    <div class="mb-10 flex items-center justify-between">
      <div>
        <h2 class="text-3xl font-black text-gray-900 flex items-center gap-3">
          <div class="p-2 bg-slate-900 rounded-xl text-white shadow-lg shadow-slate-200">
            <Settings2 class="w-6 h-6" />
          </div>
          System Settings
        </h2>
        <p class="text-sm text-gray-500 mt-2 font-medium uppercase tracking-wider">Configure System Core Codes & Classifications</p>
      </div>
    </div>

    <!-- Tabs -->
    <div class="flex space-x-1 bg-gray-100 p-1.5 rounded-2xl mb-8 w-fit">
      <button 
        @click="activeTab = 'categories'"
        :class="[
          'px-6 py-2.5 rounded-xl text-xs font-black uppercase tracking-widest transition-all',
          activeTab === 'categories' ? 'bg-white text-blue-600 shadow-sm' : 'text-gray-400 hover:text-gray-600'
        ]"
      >
        <div class="flex items-center gap-2">
          <Tag class="w-4 h-4" />
          Categories
        </div>
      </button>
      <button 
        @click="activeTab = 'departments'"
        :class="[
          'px-6 py-2.5 rounded-xl text-xs font-black uppercase tracking-widest transition-all',
          activeTab === 'departments' ? 'bg-white text-indigo-600 shadow-sm' : 'text-gray-400 hover:text-gray-600'
        ]"
      >
        <div class="flex items-center gap-2">
          <Building2 class="w-4 h-4" />
          Departments
        </div>
      </button>
    </div>

    <!-- Content Sections -->
    <div class="grid grid-cols-1 gap-8">
      <!-- Categories Management -->
      <Transition mode="out-in" enter-active-class="transition duration-300 ease-out" enter-from-class="opacity-0 translate-y-4" leave-active-class="transition duration-200 ease-in" leave-to-class="opacity-0 translate-y-4">
        <div v-if="activeTab === 'categories'" class="bg-white rounded-3xl shadow-sm border border-gray-100 p-8">
          <div class="flex items-center justify-between mb-8">
            <div>
              <h3 class="text-xl font-bold text-gray-900">Category Management</h3>
              <p class="text-xs text-gray-400 mt-1">자산 분류를 위한 카테고리를 관리합니다.</p>
            </div>
          </div>

          <!-- Add Form -->
          <form @submit.prevent="handleAddCategory" class="flex flex-col gap-3 mb-10">
            <div class="flex gap-3">
              <div class="relative flex-1 group">
                <Tag class="absolute left-4 top-1/2 -translate-y-1/2 w-4 h-4 text-gray-400 group-focus-within:text-blue-500 transition-colors" />
                <input 
                  v-model="newCategoryName"
                  type="text" 
                  placeholder="Ex) IT Equipment, Office Supplies"
                  class="w-full bg-gray-50 border border-transparent focus:bg-white focus:border-blue-500 focus:ring-4 focus:ring-blue-500/10 rounded-2xl px-12 py-4 text-sm font-medium transition-all outline-none"
                />
              </div>
              <button 
                type="submit"
                class="px-8 bg-blue-600 text-white rounded-2xl text-xs font-black uppercase tracking-widest hover:bg-blue-700 shadow-lg shadow-blue-200 active:scale-95 transition-all flex items-center gap-2"
              >
                <Plus class="w-4 h-4" /> Add New
              </button>
            </div>
            <div class="flex items-center gap-2 px-4">
              <label class="relative inline-flex items-center cursor-pointer">
                <input type="checkbox" v-model="newCategoryUseOcr" class="sr-only peer">
                <div class="w-9 h-5 bg-gray-200 peer-focus:outline-none peer-focus:ring-4 peer-focus:ring-blue-300 rounded-full peer peer-checked:after:translate-x-full peer-checked:after:border-white after:content-[''] after:absolute after:top-[2px] after:left-[2px] after:bg-white after:border-gray-300 after:border after:rounded-full after:h-4 after:w-4 after:transition-all peer-checked:bg-blue-600"></div>
              </label>
              <span class="text-[10px] font-bold text-slate-500 uppercase tracking-widest">Require ID Verification (OCR)</span>
            </div>
          </form>

          <!-- List -->
          <div class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-4">
            <div 
              v-for="cat in categoryStore.categories" 
              :key="cat.id"
              class="group bg-white border border-gray-100 p-5 rounded-2xl flex items-center justify-between hover:border-blue-200 hover:shadow-xl hover:shadow-blue-500/5 transition-all"
            >
              <div class="min-w-0 flex-1 mr-4">
                <template v-if="editingCategoryId === cat.id">
                  <input 
                    v-model="editingCategoryName"
                    class="w-full bg-blue-50 border-b-2 border-blue-500 outline-none px-1 py-0.5 text-sm font-bold text-gray-800"
                    @keyup.enter="handleUpdateCategory(cat.id!)"
                  />
                  <div class="mt-2 flex items-center gap-2">
                    <input type="checkbox" v-model="editingCategoryUseOcr" class="rounded text-blue-600">
                    <span class="text-[9px] font-bold text-blue-600 uppercase">Use OCR</span>
                  </div>
                </template>
                <div v-else>
                  <span class="text-sm font-bold text-gray-700 truncate block">{{ cat.name }}</span>
                  <span v-if="cat.useOcr" class="inline-flex items-center gap-1 mt-1 px-1.5 py-0.5 rounded-md bg-blue-50 text-[9px] font-black text-blue-600 uppercase tracking-tighter">
                    <CheckCircle class="w-2.5 h-2.5" /> OCR Secure
                  </span>
                </div>
              </div>
              
              <div class="flex items-center gap-1">
                <template v-if="editingCategoryId === cat.id">
                  <button @click="handleUpdateCategory(cat.id!)" class="p-2 text-green-600 hover:bg-green-50 rounded-lg">
                    <Check class="w-4 h-4" />
                  </button>
                  <button @click="editingCategoryId = null" class="p-2 text-gray-400 hover:bg-gray-100 rounded-lg">
                    <X class="w-4 h-4" />
                  </button>
                </template>
                <template v-else>
                  <button @click="startEditCategory(cat)" class="p-2 text-gray-400 hover:text-blue-600 hover:bg-blue-50 rounded-lg opacity-0 group-hover:opacity-100 transition-all">
                    <Edit2 class="w-4 h-4" />
                  </button>
                  <button @click="handleDeleteCategory(cat.id!)" class="p-2 text-gray-400 hover:text-red-600 hover:bg-red-50 rounded-lg opacity-0 group-hover:opacity-100 transition-all">
                    <Trash2 class="w-4 h-4" />
                  </button>
                </template>
              </div>
            </div>
          </div>
        </div>

        <!-- Departments Management -->
        <div v-else class="bg-white rounded-3xl shadow-sm border border-gray-100 p-8">
          <div class="flex items-center justify-between mb-8">
            <div>
              <h3 class="text-xl font-bold text-gray-900">Department Management</h3>
              <p class="text-xs text-gray-400 mt-1">사용자 소속을 분류하기 위한 부서 정보를 관리합니다.</p>
            </div>
          </div>

          <!-- Add Form -->
          <form @submit.prevent="handleAddDepartment" class="flex gap-3 mb-10">
            <div class="relative flex-1 group">
              <Building2 class="absolute left-4 top-1/2 -translate-y-1/2 w-4 h-4 text-gray-400 group-focus-within:text-indigo-500 transition-colors" />
              <input 
                v-model="newDepartmentName"
                type="text" 
                placeholder="Ex) Engineering, HR, Sales"
                class="w-full bg-gray-50 border border-transparent focus:bg-white focus:border-indigo-500 focus:ring-4 focus:ring-indigo-500/10 rounded-2xl px-12 py-4 text-sm font-medium transition-all outline-none"
              />
            </div>
            <button 
              type="submit"
              class="px-8 bg-indigo-600 text-white rounded-2xl text-xs font-black uppercase tracking-widest hover:bg-indigo-700 shadow-lg shadow-indigo-200 active:scale-95 transition-all flex items-center gap-2"
            >
              <Plus class="w-4 h-4" /> Add New
            </button>
          </form>

          <!-- List -->
          <div class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-4">
            <div 
              v-for="dept in departmentStore.departments" 
              :key="dept.id"
              class="group bg-white border border-gray-100 p-5 rounded-2xl flex items-center justify-between hover:border-indigo-200 hover:shadow-xl hover:shadow-indigo-500/5 transition-all"
            >
              <div class="min-w-0 flex-1 mr-4">
                <template v-if="editingDepartmentId === dept.id">
                  <input 
                    v-model="editingDepartmentName"
                    class="w-full bg-indigo-50 border-b-2 border-indigo-500 outline-none px-1 py-0.5 text-sm font-bold text-indigo-800"
                    @keyup.enter="handleUpdateDepartment(dept.id!)"
                  />
                </template>
                <span v-else class="text-sm font-bold text-gray-700 truncate block">{{ dept.name }}</span>
              </div>
              
              <div class="flex items-center gap-1">
                <template v-if="editingDepartmentId === dept.id">
                  <button @click="handleUpdateDepartment(dept.id!)" class="p-2 text-green-600 hover:bg-green-50 rounded-lg">
                    <Check class="w-4 h-4" />
                  </button>
                  <button @click="editingDepartmentId = null" class="p-2 text-gray-400 hover:bg-gray-100 rounded-lg">
                    <X class="w-4 h-4" />
                  </button>
                </template>
                <template v-else>
                  <button @click="startEditDepartment(dept)" class="p-2 text-gray-400 hover:text-indigo-600 hover:bg-indigo-50 rounded-lg opacity-0 group-hover:opacity-100 transition-all">
                    <Edit2 class="w-4 h-4" />
                  </button>
                  <button @click="handleDeleteDepartment(dept.id!)" class="p-2 text-gray-400 hover:text-red-600 hover:bg-red-50 rounded-lg opacity-0 group-hover:opacity-100 transition-all">
                    <Trash2 class="w-4 h-4" />
                  </button>
                </template>
              </div>
            </div>
          </div>
        </div>
      </Transition>
    </div>
  </div>
</template>
