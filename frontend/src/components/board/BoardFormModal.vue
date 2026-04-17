<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { X, Send } from 'lucide-vue-next'
import { useBoardStore, type BoardPost } from '../../stores/boardStore'
import { QuillEditor } from '@vueup/vue-quill'
import '@vueup/vue-quill/dist/vue-quill.snow.css'

const props = defineProps<{
  post?: BoardPost | null
}>()

const emit = defineEmits(['close'])
const boardStore = useBoardStore()

const title = ref('')
const content = ref('')
const loading = ref(false)

// 에디터 툴바 설정
const toolbarOptions = [
  ['bold', 'italic', 'underline', 'strike'],
  ['blockquote', 'code-block'],
  [{ 'header': 1 }, { 'header': 2 }],
  [{ 'list': 'ordered'}, { 'list': 'bullet' }],
  [{ 'color': [] }, { 'background': [] }],
  ['clean']
]

onMounted(() => {
  if (props.post) {
    title.value = props.post.title
    content.value = props.post.content
  }
})

const handleSubmit = async () => {
  // Quill에서 빈 내용(예: <p><br></p>) 처리 체크
  const isContentEmpty = !content.value || content.value.trim() === '<p><br></p>'
  if (!title.value.trim() || isContentEmpty) return

  loading.value = true
  try {
    if (props.post?.id) {
      await boardStore.updatePost(props.post.id, {
        title: title.value,
        content: content.value
      })
    } else {
      await boardStore.createPost({
        title: title.value,
        content: content.value
      })
    }
    emit('close')
  } catch (error) {
    console.error('Failed to save post', error)
  } finally {
    loading.value = false
  }
}
</script>

<template>
  <div class="fixed inset-0 z-50 flex items-center justify-center p-4 bg-black/40 backdrop-blur-sm">
    <div class="bg-white rounded-2xl shadow-xl w-full max-w-3xl overflow-hidden animate-in fade-in zoom-in duration-200">
      <div class="flex items-center justify-between px-6 py-4 border-b border-gray-100 bg-gray-50/50">
        <h3 class="text-lg font-bold text-gray-800">
          {{ post ? '게시글 수정' : '새 게시글 작성' }}
        </h3>
        <button @click="emit('close')" class="p-2 text-gray-400 hover:text-gray-600 rounded-full transition-colors">
          <X class="w-5 h-5" />
        </button>
      </div>

      <div class="p-6 space-y-5">
        <div class="space-y-1.5">
          <label class="text-xs font-bold text-gray-400 uppercase tracking-wider ml-1">제목</label>
          <input
            v-model="title"
            type="text"
            placeholder="제목을 입력하세요"
            class="w-full px-4 py-3 bg-gray-50 border border-transparent rounded-xl focus:ring-2 focus:ring-blue-500/20 focus:border-blue-500 focus:bg-white outline-none transition-all font-bold text-gray-800"
            required
          />
        </div>

        <div class="space-y-1.5">
          <label class="text-xs font-bold text-gray-400 uppercase tracking-wider ml-1">내용</label>
          <div class="quill-wrapper">
            <QuillEditor
              v-model:content="content"
              contentType="html"
              theme="snow"
              :toolbar="toolbarOptions"
              placeholder="내용을 입력하세요..."
              class="min-h-[300px] max-h-[500px] overflow-y-auto"
            />
          </div>
        </div>
      </div>

      <div class="px-6 py-4 bg-gray-50 border-t border-gray-100 flex items-center justify-end space-x-3">
        <button
          @click="emit('close')"
          class="px-4 py-2 text-sm font-semibold text-gray-600 hover:bg-gray-200 rounded-xl transition-colors"
        >
          취소
        </button>
        <button
          @click="handleSubmit"
          :disabled="loading || !title.trim()"
          class="flex items-center space-x-2 px-8 py-2.5 bg-blue-600 text-white text-sm font-bold rounded-xl hover:bg-blue-700 transition-all shadow-lg shadow-blue-200 active:scale-95 disabled:opacity-50"
        >
          <Send v-if="!loading" class="w-4 h-4" />
          <span>{{ loading ? '저장 중...' : '저장하기' }}</span>
        </button>
      </div>
    </div>
  </div>
</template>

<style>
/* Quill Editor Customization */
.quill-wrapper .ql-container {
  border-bottom-left-radius: 0.75rem;
  border-bottom-right-radius: 0.75rem;
  font-family: inherit;
  font-size: 0.95rem;
}
.quill-wrapper .ql-toolbar {
  border-top-left-radius: 0.75rem;
  border-top-right-radius: 0.75rem;
  background: #f8fafc;
  border-color: #e2e8f0 !important;
}
.quill-wrapper .ql-container.ql-snow {
  border-color: #e2e8f0 !important;
}
.ql-editor {
  min-height: 300px;
}
.ql-editor.ql-blank::before {
  color: #94a3b8;
  font-style: normal;
}
</style>
