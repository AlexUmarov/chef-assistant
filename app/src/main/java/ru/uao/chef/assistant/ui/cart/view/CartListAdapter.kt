package ru.uao.chef.assistant.ui.cart.view

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import ru.uao.chef.assistant.R
import ru.uao.chef.assistant.ui.product.data.Product


class CartListAdapter(
    private val productList: ArrayList<Product>,
    private val listener: OnItemClickListener
) : RecyclerView.Adapter<CartListAdapter.CartViewHolder>() {

    interface OnItemClickListener {
        fun onItemClick(position: Int)
    }

    inner class CartViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView),
        View.OnClickListener {
        var productName: TextView = itemView.findViewById(R.id.productNameInfo)
        var weightProduct: TextView = itemView.findViewById(R.id.weightProductInfo)
        var priceProduct: TextView = itemView.findViewById(R.id.priceProductInfo)

        init {
            itemView.setOnClickListener(this)
        }

        override fun onClick(v: View?) {
            val position: Int = adapterPosition
            if (position != RecyclerView.NO_POSITION) {
                listener.onItemClick(position)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CartViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val v = inflater.inflate(R.layout.list_cart_item, parent, false)
        return CartViewHolder(v)
    }

    override fun onBindViewHolder(holder: CartViewHolder, position: Int) {
        val product = productList[position]
        holder.productName.text = product.productName
        holder.weightProduct.text = product.productWeight.toString()
        holder.priceProduct.text = product.productPrice.toString()

    }

    override fun getItemCount(): Int {
        return productList.size
    }
}