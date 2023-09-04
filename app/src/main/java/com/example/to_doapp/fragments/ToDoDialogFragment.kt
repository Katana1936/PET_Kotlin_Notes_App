package com.example.to_doapp.fragments
import android.content.res.Resources
import android.graphics.Color
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import com.example.to_doapp.R
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
    override fun onStart() {
        super.onStart()
        dialog?.let {
            val width = 350.toPx()
            val height = 250.toPx()
            it.window?.setLayout(width, height)
            it.window?.setDimAmount(0.1f)
            it.window?.setBackgroundDrawableResource(R.drawable.rounded_background_fragment_dialog)
        }
    }
    fun Int.toPx(): Int = (this * Resources.getSystem().displayMetrics.density).toInt()
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
            binding.EditText.setText(toDoData?.task)
        }
        binding.todoClose.setOnClickListener {
            dismiss()
        }
        binding.todoNextBtn.setOnClickListener {
            val todoTask = binding.EditText.text.toString()
            if (todoTask.isNotEmpty()){
                if (toDoData == null){
                    listener?.saveTask(todoTask , binding.EditText)
                } else {
                    toDoData!!.task = todoTask
                    listener?.updateTask(toDoData!!, binding.EditText)
                }
            }
        }
        binding.EditText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                //
            }
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                //
            }
            override fun afterTextChanged(s: Editable?) {
                val str = s.toString()
                if (str.length > 300) {
                    s?.delete(300, str.length)
                    Toast.makeText(context, "Превышен лимит символов", Toast.LENGTH_SHORT).show()
                }
                var lineBreaks = 0
                for (i in 0 until str.length) {
                    if (i > 0 && (i + lineBreaks) % 25 == 0) {
                        s?.insert(i + lineBreaks, "\n")
                        lineBreaks++
                    }
                }
            }
        })
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