package ru.uao.chef.assistant.ui.product.view

import android.view.View
import androidx.recyclerview.widget.RecyclerView

class RecyclerViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView), View.OnClickListener, View.OnLongClickListener {

    init{
        itemView.setOnClickListener(this)
        itemView.setOnLongClickListener(this)
    }

    override fun onClick(v: View?) {
        TODO("Not yet implemented")
    }

    override fun onLongClick(v: View?): Boolean {
        TODO("Not yet implemented")
    }
}