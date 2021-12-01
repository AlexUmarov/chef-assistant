package ru.uao.chef.assistant.ui.home.view

import android.app.AlertDialog
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.recyclerview.widget.RecyclerView
import ru.uao.chef.assistant.R
import ru.uao.chef.assistant.ui.home.data.Cart


class CartListAdapter(private val c:Context,
                      private val cartList:ArrayList<Cart>,
                      private val cartDeleteList:ArrayList<Cart>,
                      private val listener: OnItemClickListener
):RecyclerView.Adapter<CartListAdapter.CartViewHolder>()
{
    interface OnItemClickListener{
        fun onItemClick(position: Int)
    }

    inner class CartViewHolder(itemView:View):RecyclerView.ViewHolder(itemView), View.OnClickListener{
        var cartName:TextView = itemView.findViewById(R.id.productNameInfo)

        var mMenus:ImageView = itemView.findViewById(R.id.mMenus)

        init {
            mMenus.setOnClickListener { popupMenus(it) }
            itemView.setOnClickListener(this)
        }

        override fun onClick(v: View?) {
            val position: Int = adapterPosition
            if(position != RecyclerView.NO_POSITION) {
                listener.onItemClick(position)
            }
        }

        private fun popupMenus(v:View) {
            val position = cartList[adapterPosition]
            val popupMenus = PopupMenu(c,v)
            popupMenus.inflate(R.menu.show_menu)
            popupMenus.setOnMenuItemClickListener {
                when(it.itemId){
                    R.id.editText->{
                        val viewSource = LayoutInflater.from(c).inflate(R.layout.add_product_item,null)
                        val productName = viewSource.findViewById<EditText>(R.id.productName)
                        productName.setText(position.cartName)
                        AlertDialog.Builder(c)
                            .setView(viewSource)
                            .setPositiveButton("Ok"){
                                    dialog,_->
                                position.cartName = productName.text.toString()
                                notifyDataSetChanged()
                                dialog.dismiss()
                            }
                            .setNegativeButton("Cancel"){
                                    dialog,_->
                                dialog.dismiss()

                            }
                            .create()
                            .show()

                        true
                    }
                    R.id.delete->{
                        /**set delete*/
                        AlertDialog.Builder(c)
                            .setTitle("Delete")
                            .setIcon(R.drawable.ic_warning)
                            .setMessage("Are you sure delete this Information")
                            .setPositiveButton("Yes"){
                                    dialog,_->
                                cartDeleteList.add(cartList[adapterPosition])
                                cartList.removeAt(adapterPosition)
                                notifyDataSetChanged()
                                Toast.makeText(c,"Deleted this Information",Toast.LENGTH_SHORT).show()
                                dialog.dismiss()
                            }
                            .setNegativeButton("No"){
                                    dialog,_->
                                dialog.dismiss()
                            }
                            .create()
                            .show()

                        true
                    }
                    else-> true
                }

            }
            popupMenus.show()
            val popup = PopupMenu::class.java.getDeclaredField("mPopup")
            popup.isAccessible = true
            val menu = popup.get(popupMenus)
            menu.javaClass.getDeclaredMethod("setForceShowIcon",Boolean::class.java)
                .invoke(menu,true)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CartViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val v  = inflater.inflate(R.layout.list_product_item,parent,false)
        return CartViewHolder(v)
    }

    override fun  onBindViewHolder(holder: CartViewHolder, position: Int) {
        val cart = cartList[position]
        holder.cartName.text = cart.cartName
    }

    override fun getItemCount(): Int {
        return  cartList.size
    }
}