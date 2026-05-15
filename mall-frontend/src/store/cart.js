import { defineStore } from 'pinia'
import { getCartCount } from '../api/cart'

export const useCartStore = defineStore('cart', {
  state: () => ({
    count: 0
  }),
  actions: {
    async refreshCount() {
      try {
        const data = await getCartCount()
        this.count = data?.count || 0
      } catch {
        this.count = 0
      }
    }
  }
})

