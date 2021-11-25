package ru.uao.chef.assistant.ui.product.view

import android.app.AlertDialog
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.recyclerview.widget.RecyclerView
import ru.uao.chef.assistant.R
import ru.uao.chef.assistant.ui.product.data.Product

class ProductAdapter(val c:Context, val productAddList:ArrayList<Product>,
                     val productDeleteList:ArrayList<Product>):RecyclerView.Adapter<ProductAdapter.WorkoutViewHolder>()
{
    inner class WorkoutViewHolder(v:View):RecyclerView.ViewHolder(v){
        var productName:TextView = v.findViewById(R.id.productNameInfo)
        var weightProduct:TextView = v.findViewById(R.id.weightProductInfo)
        var priceProduct:TextView = v.findViewById(R.id.priceProductInfo)

        var mMenus:ImageView = v.findViewById(R.id.mMenus)

        init {
            mMenus.setOnClickListener { popupMenus(it) }
        }

        private fun popupMenus(v:View) {
            val position = productAddList[adapterPosition]
            val popupMenus = PopupMenu(c,v)
            popupMenus.inflate(R.menu.show_menu)
            popupMenus.setOnMenuItemClickListener {
                when(it.itemId){
                    R.id.editText->{
                        val viewSource = LayoutInflater.from(c).inflate(R.layout.add_product_item,null)
                        val productName = viewSource.findViewById<EditText>(R.id.productName)
                        productName.setText(position.productName)
                        val weightProduct = viewSource.findViewById<EditText>(R.id.weightProduct)
                        weightProduct.setText(position.productWeight.toString())
                        val priceProduct = viewSource.findViewById<EditText>(R.id.priceProduct)
                        priceProduct.setText(position.productPrice.toString())
                        AlertDialog.Builder(c)
                            .setView(viewSource)
                            .setPositiveButton("Ok"){
                                    dialog,_->
                                position.productName = productName.text.toString()
                                position.productWeight = weightProduct.text.toString().toFloat()
                                position.productPrice = priceProduct.text.toString().toFloat()
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
                                productDeleteList.add(productAddList[adapterPosition])
                                productAddList.removeAt(adapterPosition)
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

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): WorkoutViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val v  = inflater.inflate(R.layout.list_product_item,parent,false)
        return WorkoutViewHolder(v)
    }

    override fun onBindViewHolder(holder: WorkoutViewHolder, position: Int) {
        val newList = productAddList[position]
        holder.productName.text = newList.productName
        holder.weightProduct.text = newList.productWeight.toString()
        holder.priceProduct.text = newList.productPrice.toString()
    }

    override fun getItemCount(): Int {
        return  productAddList.size
    }
}