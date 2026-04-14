<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { Plus, Edit2, Trash2, Tag, X, Check } from 'lucide-vue-next'
import { useCategoryStore } from '../stores/categoryStore'

const categoryStore = useCategoryStore()
const newCategoryName = ref('')
const editingId = ref<number | null>(null)
const editingName = ref('')

onMounted(() => {
  categoryStore.fetchCategories()
})

const handleAdd = async () => {
  if (!newCategoryName.value.trim()) return
  if (newCategoryName.value.length > 50) {
    alert('Category name is too long (max 50 characters)')
    return
  }
  try {
    await categoryStore.addCategory(newCategoryName.value, false)
    newCategoryName.value = ''
  } catch (err: any) {
    const errorMsg = err.response?.data?.message || err.message || 'Failed to add category'
    alert(errorMsg)
  }
}

const startEdit = (category: any) => {
  editingId.value = category.id
  editingName.value = category.name
}

const cancelEdit = () => {
  editingId.value = null
  editingName.value = ''
}

const handleUpdate = async (id: number) => {
  if (!editingName.value.trim()) return
  try {
    await categoryStore.updateCategory(id, editingName.value, false)
    editingId.value = null
  } catch (err: any) {
    alert(err.message || 'Failed to update category')
  }
}

const handleDelete = async (id: number) => {
  if (confirm('Are you sure? Removing a category might affect assets linked to it.')) {
    try {
      await categoryStore.deleteCategory(id)
    } catch (err: any) {
      alert(err.message || 'Failed to delete category')
    }
  }
}
</script>

<template>
  <div class="max-w-4xl mx-auto space-y-6">
    <div class="bg-white p-6 rounded-2xl shadow-sm border border-gray-100">
      <h2 class="text-xl font-bold text-gray-800 mb-6 flex items-center">
        <Tag class="w-5 h-5 mr-2 text-blue-600" />
        Category Management
      </h2>

      <!-- Add Category Form -->
      <form @submit.prevent="handleAdd" class="flex space-x-3 mb-8">
        <input
          v-model="newCategoryName"
          type="text"
          maxlength="50"
          placeholder="New category name (e.g. Software, Furniture)"
          class="flex-1 px-4 py-2 bg-gray-50 border border-gray-100 rounded-xl outline-none focus:ring-2 focus:ring-blue-500/20 focus:border-blue-500 transition-all font-medium"
        />
        <button
          type="submit"
          class="flex items-center space-x-2 px-6 py-2 bg-blue-600 text-white rounded-xl hover:bg-blue-700 transition-all shadow-lg shadow-blue-100 active:scale-95"
        >
          <Plus class="w-5 h-5" />
          <span class="font-semibold">Add</span>
        </button>
      </form>

      <!-- Categories List -->
      <div v-if="categoryStore.loading" class="text-center py-12 text-gray-400">
        Loading categories...
      </div>
      <div v-else class="grid grid-cols-1 md:grid-cols-2 gap-4">
        <div 
          v-for="category in categoryStore.categories" 
          :key="category.id"
          class="flex items-center justify-between p-4 bg-white border border-gray-100 rounded-2xl hover:shadow-md hover:border-blue-100 transition-all group"
        >
          <div class="flex-1 mr-4 min-w-0">
            <template v-if="editingId === category.id">
              <input 
                v-model="editingName"
                maxlength="50"
                @keyup.enter="handleUpdate(category.id!)"
                class="w-full bg-blue-50/50 border-b-2 border-blue-500 outline-none px-1 py-0.5 font-bold text-gray-800"
              />
            </template>
            <template v-else>
              <h4 class="font-bold text-gray-800 truncate" :title="category.name">{{ category.name }}</h4>
            </template>
          </div>

          <div class="flex items-center space-x-1">
            <template v-if="editingId === category.id">
              <button @click="handleUpdate(category.id!)" class="p-2 text-green-600 hover:bg-green-50 rounded-lg transition-colors" title="Save">
                <Check class="w-4 h-4" />
              </button>
              <button @click="cancelEdit" class="p-2 text-gray-400 hover:bg-gray-100 rounded-lg transition-colors" title="Cancel">
                <X class="w-4 h-4" />
              </button>
            </template>
            <template v-else>
              <button @click="startEdit(category)" class="p-2 text-gray-400 hover:text-blue-600 hover:bg-blue-50 rounded-lg transition-colors opacity-0 group-hover:opacity-100" title="Edit">
                <Edit2 class="w-4 h-4" />
              </button>
              <button @click="handleDelete(category.id!)" class="p-2 text-gray-400 hover:text-red-600 hover:bg-red-50 rounded-lg transition-colors opacity-0 group-hover:opacity-100" title="Delete">
                <Trash2 class="w-4 h-4" />
              </button>
            </template>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>
