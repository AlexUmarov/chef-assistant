package ru.uao.chef.assistant.ui.home.view

import android.app.AlertDialog
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.recyclerview.widget.RecyclerView
import ru.uao.chef.assistant.R
import ru.uao.chef.assistant.ui.home.data.Recipe


class RecipeListAdapter(private val c:Context,
                        private val recipeList:ArrayList<Recipe>,
                        private val recipeDeleteList:ArrayList<Recipe>,
                        private val listener: OnItemClickListener
):RecyclerView.Adapter<RecipeListAdapter.RecipeViewHolder>()
{
    interface OnItemClickListener{
        fun onItemClick(position: Int)
    }

    inner class RecipeViewHolder(itemView:View):RecyclerView.ViewHolder(itemView), View.OnClickListener{
        var recipeName:TextView = itemView.findViewById(R.id.recipeNameInfo)

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
            val position = recipeList[adapterPosition]
            val popupMenus = PopupMenu(c,v)
            popupMenus.inflate(R.menu.show_menu)
            popupMenus.setOnMenuItemClickListener {
                when(it.itemId){
                    R.id.editText->{
                        val viewSource = LayoutInflater.from(c).inflate(R.layout.add_recipe_item,null)
                        val recipeName = viewSource.findViewById<EditText>(R.id.recipeName)
                        recipeName.setText(position.recipeName)
                        AlertDialog.Builder(c)
                            .setView(viewSource)
                            .setPositiveButton("Ok"){
                                    dialog,_->
                                position.recipeName = recipeName.text.toString()
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
                                recipeDeleteList.add(recipeList[adapterPosition])
                                recipeList.removeAt(adapterPosition)
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

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecipeViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val v  = inflater.inflate(R.layout.list_recipe_item,parent,false)
        return RecipeViewHolder(v)
    }

    override fun  onBindViewHolder(holder: RecipeViewHolder, position: Int) {
        val recipe = recipeList[position]
        holder.recipeName.text = recipe.recipeName
    }

    override fun getItemCount(): Int {
        return  recipeList.size
    }
}