package ru.uao.chef.assistant.ui.home.data

import ru.uao.chef.assistant.ui.product.data.Product

class Recipe(var recipeName : String,
             var cost: Float,
             var products: ArrayList<Product>
              ){
    fun getTotalCostProduct(): Float{
        var res = 0.0F
        for(i in products.indices){
            res += products[i].productPrice
        }
        return "%.2f".format(res).toFloat()
    }

    fun getTotalWeightProduct(): Float{
        var res = 0.0F
        for(i in products.indices){
            res += products[i].productWeight
        }
        return res
    }
}