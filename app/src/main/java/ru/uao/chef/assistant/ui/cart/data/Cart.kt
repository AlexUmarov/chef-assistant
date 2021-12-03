package ru.uao.chef.assistant.ui.cart.data

import ru.uao.chef.assistant.ui.product.data.Product

class Cart(var cartName : String,
           var totalCost: Float,
           var products: ArrayList<Product>
              ){

    fun getTotalCostProduct(): Float{
        var res = 0.0F
        for(i in products.indices){
            res += products[i].productPrice
        }
        return "%.2f".format(res).toFloat()
    }
}