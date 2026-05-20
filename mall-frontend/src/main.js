import { createApp } from 'vue'
import { createPinia } from 'pinia'
import { ElButton } from 'element-plus/es/components/button/index.mjs'
import { ElCard } from 'element-plus/es/components/card/index.mjs'
import { ElCheckbox } from 'element-plus/es/components/checkbox/index.mjs'
import { ElAside, ElContainer, ElHeader, ElMain } from 'element-plus/es/components/container/index.mjs'
import { ElDescriptions, ElDescriptionsItem } from 'element-plus/es/components/descriptions/index.mjs'
import { ElDialog } from 'element-plus/es/components/dialog/index.mjs'
import { ElDrawer } from 'element-plus/es/components/drawer/index.mjs'
import { ElEmpty } from 'element-plus/es/components/empty/index.mjs'
import { ElForm, ElFormItem } from 'element-plus/es/components/form/index.mjs'
import { ElIcon } from 'element-plus/es/components/icon/index.mjs'
import { ElInput } from 'element-plus/es/components/input/index.mjs'
import { ElInputNumber } from 'element-plus/es/components/input-number/index.mjs'
import { ElLoading } from 'element-plus/es/components/loading/index.mjs'
import { ElMenu, ElMenuItem } from 'element-plus/es/components/menu/index.mjs'
import { ElPagination } from 'element-plus/es/components/pagination/index.mjs'
import { ElRadio, ElRadioGroup } from 'element-plus/es/components/radio/index.mjs'
import { ElOption, ElSelect } from 'element-plus/es/components/select/index.mjs'
import { ElSkeleton, ElSkeletonItem } from 'element-plus/es/components/skeleton/index.mjs'
import { ElSwitch } from 'element-plus/es/components/switch/index.mjs'
import { ElTable, ElTableColumn } from 'element-plus/es/components/table/index.mjs'
import { ElTag } from 'element-plus/es/components/tag/index.mjs'
import { ElTimeline, ElTimelineItem } from 'element-plus/es/components/timeline/index.mjs'
import { ElUpload } from 'element-plus/es/components/upload/index.mjs'
import {
  Collection,
  DataBoard,
  Document,
  Goods,
  Search,
  ShoppingBag,
  Tickets,
  User as UserIcon
} from '@element-plus/icons-vue'
import 'element-plus/dist/index.css'
import './assets/main.css'
import App from './App.vue'
import router from './router'

const app = createApp(App)

const elementComponents = [
  ElAside,
  ElButton,
  ElCard,
  ElCheckbox,
  ElContainer,
  ElDescriptions,
  ElDescriptionsItem,
  ElDialog,
  ElDrawer,
  ElEmpty,
  ElForm,
  ElFormItem,
  ElHeader,
  ElIcon,
  ElInput,
  ElInputNumber,
  ElLoading,
  ElMain,
  ElMenu,
  ElMenuItem,
  ElOption,
  ElPagination,
  ElRadio,
  ElRadioGroup,
  ElSelect,
  ElSkeleton,
  ElSkeletonItem,
  ElSwitch,
  ElTable,
  ElTableColumn,
  ElTag,
  ElTimeline,
  ElTimelineItem,
  ElUpload
]

for (const component of elementComponents) {
  app.use(component)
}

app.component('Collection', Collection)
app.component('DataBoard', DataBoard)
app.component('Document', Document)
app.component('Goods', Goods)
app.component('Search', Search)
app.component('ShoppingBag', ShoppingBag)
app.component('Tickets', Tickets)
app.component('User', UserIcon)

app.use(createPinia())
app.use(router)
app.mount('#app')
