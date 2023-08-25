package com.example.to_doapp.fragments

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.SearchView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.to_doapp.databinding.FragmentHomeBinding
import com.example.to_doapp.utils.adapter.TaskAdapter
import com.example.to_doapp.utils.model.ToDoData
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase

class HomeFragment : Fragment(), ToDoDialogFragment.OnDialogNextBtnClickListener,
    TaskAdapter.TaskAdapterInterface {
    private val TAG = "HomeFragment"
    private lateinit var binding: FragmentHomeBinding
    private lateinit var database: DatabaseReference
    private var frag: ToDoDialogFragment? = null
    private lateinit var auth: FirebaseAuth
    private lateinit var authId: String
    private lateinit var taskAdapter: TaskAdapter
    private lateinit var toDoItemList: MutableList<ToDoData>

    private var isSearchViewEnabled = true

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }
    @SuppressLint("ClickableViewAccessibility")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        init()
        getTaskFromFirebase()
        binding.addTaskBtnMain.setOnClickListener {
            if (frag != null)
                childFragmentManager.beginTransaction().remove(frag!!).commit()
            frag = ToDoDialogFragment()
            frag!!.setListener(this)
            frag!!.show(
                childFragmentManager,
                ToDoDialogFragment.TAG
            )
        }

        binding.Edit.setOnClickListener {
            toggleEditMode()
        }

        binding.Done.setOnClickListener {
            toggleEditMode()
        }

        binding.searchView.setOnTouchListener { _, _ ->
            !isSearchViewEnabled
        }

        binding.searchView.setOnQueryTextFocusChangeListener { _, hasFocus ->
            if (!isSearchViewEnabled && hasFocus) {
                binding.searchView.clearFocus()
            }
        }
        binding.Done.setOnClickListener {
            taskAdapter.deselectAllItems()
            taskAdapter.toggleSelectionMode()
            isSearchViewEnabled = true
            // остальные действия, которые необходимо выполнить при нажатии на Done
        }

    }

    private fun toggleEditMode() {
        taskAdapter.toggleSelectionMode()
        isSearchViewEnabled = !isSearchViewEnabled
        binding.searchView.clearFocus()
        binding.searchView.alpha = if (isSearchViewEnabled) 1.0f else 0.5f
        binding.MoveAll.visibility = if (binding.MoveAll.visibility == View.VISIBLE) View.GONE else View.VISIBLE
        binding.DeleteAll.visibility = if (binding.DeleteAll.visibility == View.VISIBLE) View.GONE else View.VISIBLE
        binding.noteNum.visibility = if (binding.noteNum.visibility == View.VISIBLE) View.GONE else View.VISIBLE
        binding.addTaskBtnMain.visibility = if (binding.addTaskBtnMain.visibility == View.VISIBLE) View.GONE else View.VISIBLE
        binding.Done.visibility = if (binding.Done.visibility == View.VISIBLE) View.GONE else View.VISIBLE
        binding.Edit.visibility = if (binding.Done.visibility == View.VISIBLE) View.GONE else View.VISIBLE
    }

    private fun getTaskFromFirebase() {
        database.addValueEventListener(object : ValueEventListener {
            @SuppressLint("NotifyDataSetChanged")
            override fun onDataChange(snapshot: DataSnapshot) {
                toDoItemList.clear()
                for (taskSnapshot in snapshot.children) {
                    val taskId = taskSnapshot.key ?: continue
                    val task = taskSnapshot.child("task").getValue(String::class.java) ?: ""
                    val timestamp = taskSnapshot.child("timestamp").getValue(Long::class.java) ?: continue
                    val todoTask = ToDoData(taskId, task, timestamp)
                    toDoItemList.add(todoTask)
                }
                taskAdapter.notifyDataSetChanged()
            }
            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(context, error.toString(), Toast.LENGTH_SHORT).show()
            }
        })
    }
    private fun init() {
        auth = FirebaseAuth.getInstance()
        authId = auth.currentUser!!.uid
        database = Firebase.database.reference.child("Tasks")
            .child(authId)
        binding.mainRecyclerView.setHasFixedSize(true)
        binding.mainRecyclerView.layoutManager = LinearLayoutManager(context)
        toDoItemList = mutableListOf()
        taskAdapter = TaskAdapter(toDoItemList)
        taskAdapter.setListener(this)
        binding.mainRecyclerView.adapter = taskAdapter
        //new
        taskAdapter.registerAdapterDataObserver(object : RecyclerView.AdapterDataObserver() {
            override fun onChanged() {
                updateNoteCount()
            }
            override fun onItemRangeInserted(positionStart: Int, itemCount: Int) {
                updateNoteCount()
            }
            override fun onItemRangeRemoved(positionStart: Int, itemCount: Int) {
                updateNoteCount()
            }
        })
        binding.searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                return false
            }
            override fun onQueryTextChange(newText: String?): Boolean {
                filterTasks(newText ?: "")
                return true
            }
        })
    }

    private fun filterTasks(query: String) {
        val filteredList = toDoItemList.filter { it.task.contains(query, ignoreCase = true) }
        taskAdapter.updateList(filteredList)
    }

    private fun updateNoteCount() {
        val itemCount = taskAdapter.itemCount
        binding.noteNum.text = when (itemCount) {
            0 -> "No Notes"
            1 -> "1 Note"
            else -> "$itemCount Notes"
        }
    }

    override fun saveTask(todoTask: String, todoEdit: TextInputEditText) {
        val taskId = database.push().key ?: return
        val newTask = ToDoData(taskId, todoTask)
        database.child(taskId).setValue(newTask).addOnCompleteListener {
            if (it.isSuccessful) {
                Toast.makeText(context, "Task Added Successfully", Toast.LENGTH_SHORT).show()
                todoEdit.text = null
            } else {
                Toast.makeText(context, it.exception.toString(), Toast.LENGTH_SHORT).show()
            }
        }
        frag!!.dismiss()
    }

    override fun updateTask(toDoData: ToDoData, todoEdit: TextInputEditText) {
        val map = HashMap<String, Any>()
        map[toDoData.taskId] = toDoData.task
        database.updateChildren(map).addOnCompleteListener {
            if (it.isSuccessful) {
                Toast.makeText(context, "Updated Successfully", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(context, it.exception.toString(), Toast.LENGTH_SHORT).show()
            }
            frag!!.dismiss()
        }
    }
    override fun onDeleteItemClicked(toDoData: ToDoData, position: Int) {
        database.child(toDoData.taskId).removeValue().addOnCompleteListener {
            if (it.isSuccessful) {
                Toast.makeText(context, "Deleted Successfully", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(context, it.exception.toString(), Toast.LENGTH_SHORT).show()
            }
        }
    }
    override fun onEditItemClicked(toDoData: ToDoData, position: Int) {
        if (frag != null)
            childFragmentManager.beginTransaction().remove(frag!!).commit()
        frag = ToDoDialogFragment.newInstance(toDoData.taskId, toDoData.task)
        frag!!.setListener(this)
        frag!!.show(
            childFragmentManager,
            ToDoDialogFragment.TAG
        )
    }

}