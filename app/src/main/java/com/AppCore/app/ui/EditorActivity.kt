package com.AppCore.app.ui

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.AppCore.app.R
import androidx.databinding.DataBindingUtil
import com.AppCore.app.databinding.ActivityEditorBinding

class EditorActivity : AppCompatActivity() {
    private lateinit var binding: ActivityEditorBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_editor)
    }
}
