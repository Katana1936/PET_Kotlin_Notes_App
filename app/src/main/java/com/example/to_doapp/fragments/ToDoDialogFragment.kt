package com.example.to_doapp.fragments
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.DialogFragment
import com.example.to_doapp.databinding.FragmentToDoDialogBinding
import com.example.to_doapp.utils.model.ToDoData
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
class ToDoDialogFragment : DialogFragment() {
    private lateinit var binding: FragmentToDoDialogBinding
    private var listener : OnDialogNextBtnClickListener? = null
    private var toDoData: ToDoData? = null
    fun setListener(listener: OnDialogNextBtnClickListener) {
        this.listener = listener
    }
    companion object {
        const val TAG = "DialogFragment"
        @JvmStatic
        fun newInstance(taskId: String, task: String) =
            ToDoDialogFragment().apply {
                arguments = Bundle().apply {
                    putString("taskId", taskId)
                    putString("task", task)
                }
            }
    }
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentToDoDialogBinding.inflate(inflater , container,false)
        return binding.root
    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        if (arguments != null){
            toDoData = ToDoData(arguments?.getString("taskId").toString() ,arguments?.getString("task").toString())
            binding.todoEt.setText(toDoData?.task)
        }
        binding.todoClose.setOnClickListener {
            dismiss()
        }
        binding.todoNextBtn.setOnClickListener {
            val todoTask = binding.todoEt.text.toString()
            if (todoTask.isNotEmpty()){
                if (toDoData == null){
                    listener?.saveTask(todoTask , binding.todoEt)
                }else{
                    toDoData!!.task = todoTask
                    listener?.updateTask(toDoData!!, binding.todoEt)
                }
            }
        }
        val currentDate = Calendar.getInstance().time
        val formatter = SimpleDateFormat("EEE, dd MMM yyyy", Locale.ENGLISH)
        val formattedDate = formatter.format(currentDate)
        binding.date.text = formattedDate
        binding.date.setTextColor(Color.WHITE)
    }
    interface OnDialogNextBtnClickListener{
        fun saveTask(todoTask: String, todoEdit: TextView)
        fun updateTask(toDoData: ToDoData, todoEdit: TextView)
    }
}