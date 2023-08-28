package com.example.to_doapp.utils.adapter

import android.annotation.SuppressLint
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.example.to_doapp.R
import com.example.to_doapp.databinding.EachTodoItemBinding
import com.example.to_doapp.fragments.HomeFragment
import com.example.to_doapp.utils.model.ToDoData
import java.text.SimpleDateFormat
import java.util.Date

class PinnedTaskAdapter(private var list: MutableList<ToDoData>) : RecyclerView.Adapter<PinnedTaskAdapter.TaskViewHolder>() {
    private val TAG = "PinnedTaskAdapter"
    private var listener: PinnedTaskAdapterInterface? = null
    private var swipedPosition: Int = RecyclerView.NO_POSITION
    var isSelectionMode = false
    fun setListener(listener: PinnedTaskAdapterInterface) {
        this.listener = listener
    }
    class TaskViewHolder(val binding: EachTodoItemBinding) : RecyclerView.ViewHolder(binding.root)
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TaskViewHolder {
        val binding = EachTodoItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return TaskViewHolder(binding)
    }
    @SuppressLint("SimpleDateFormat")
    override fun onBindViewHolder(holder: TaskViewHolder, position: Int) {
        with(holder) {
            with(list[position]) {
                binding.todoTask.text = this.task
                val sdf = SimpleDateFormat("HH:mm")
                val date = Date(this.timestamp)
                binding.textTime.text = sdf.format(date)
                binding.selectionCircle.visibility = if (isSelectionMode) View.VISIBLE else View.GONE
                binding.selectionCircle.isChecked = this.isSelected
                binding.selectionCircle.setOnClickListener {
                    if (isSelectionMode) {
                        toggleSelection(position)
                    }
                }
                val isItemSelected = this.isSelected
                val backgroundDrawableRes = when {
                    list.size == 1 -> if (isItemSelected) R.drawable.selected_rounded_corners else R.drawable.rounded_corners
                    position == 0 -> if (isItemSelected) R.drawable.selected_top_rounded_corners else R.drawable.top_rounded_corners
                    position == list.size - 1 -> if (isItemSelected) R.drawable.selected_bottom_rounded_corners else R.drawable.bottom_rounded_corners
                    else -> if (isItemSelected) R.drawable.selected_sharp_corners else R.drawable.sharp_corners
                }
                binding.root.setBackgroundResource(backgroundDrawableRes)
                binding.editTask.setOnClickListener {
                    listener?.onEditItemClicked(this, position)
                }
                binding.deleteTask.setOnClickListener {
                    //listener?.onDeleteItemClicked(this , position)
                }
            }
        }
    }
    fun toggleSelection(position: Int) {
        list[position].isSelected = !list[position].isSelected
        Log.d(TAG, "Toggled selection for position $position: ${list[position].isSelected}")
        notifyItemChanged(position)
    }
    @SuppressLint("NotifyDataSetChanged")
    fun toggleSelectionMode() {
        isSelectionMode = !isSelectionMode
        if (!isSelectionMode) {
            list.forEach { it.isSelected = false }
        }
        notifyDataSetChanged()
    }
    override fun getItemCount(): Int {
        return list.size
    }
    @SuppressLint("NotifyDataSetChanged")
    fun updateList(newList: List<ToDoData>) {
        list = newList.toMutableList()
        list.forEach { it.isSelected = false }
        notifyDataSetChanged()
    }
    @SuppressLint("NotifyDataSetChanged")
    fun deleteSelectedItems() {
        val iterator = list.iterator()
        while (iterator.hasNext()) {
            val item = iterator.next()
            if (item.isSelected) {
                listener?.onDeleteItemClicked(item, list.indexOf(item))
                iterator.remove()
            }
        }
        notifyDataSetChanged()
    }
    interface PinnedTaskAdapterInterface {
        fun onDeleteItemClicked(toDoData: ToDoData, position: Int)
        fun onEditItemClicked(toDoData: ToDoData, position: Int)
    }
    fun removeItem(position: Int): ToDoData {
        val item = list[position]
        list.removeAt(position)
        listener?.onDeleteItemClicked(item, position)
        notifyItemRemoved(position)
        return item
    }
    fun addItem(item: ToDoData) {
        list.add(item)
        notifyItemInserted(list.size + 1)
        Log.d(TAG, "Item added. Current list: $list")
    }
}
