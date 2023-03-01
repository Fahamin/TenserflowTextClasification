package com.fahim.tenserflowtextclasification

import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.RadioGroup
import android.widget.Toast
import com.fahim.tenserflowtextclasification.databinding.ActivityMainBinding
import com.google.android.material.bottomsheet.BottomSheetBehavior
import org.tensorflow.lite.support.label.Category

class MainActivity : AppCompatActivity() {

    private var _activityMainBinding: ActivityMainBinding? = null
    private val activityMainBinding get() = _activityMainBinding!!
    private lateinit var classifierHelper: TextClassificationHelper
    private val adapter by lazy {
        ResultsAdapter()
    }

    private val listener = object : TextClassificationHelper.TextResultsListener {
        override fun onResult(results: List<Category>, inferenceTime: Long) {
            runOnUiThread {
                activityMainBinding.bottomSheetLayout.inferenceTimeVal.text =
                    String.format("%d ms", inferenceTime)

                adapter.resultsList = results.sortedByDescending {
                    it.score
                }

                adapter.notifyDataSetChanged()
            }
        }

        override fun onError(error: String) {
            Toast.makeText(this@MainActivity, error, Toast.LENGTH_SHORT).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
// Create the classification helper that will do the heavy lifting
        classifierHelper = TextClassificationHelper(
            context = this@MainActivity,
            listener = listener)

        // Classify the text in the TextEdit box (or the default if nothing is added)
        // on button click.
        activityMainBinding.classifyBtn.setOnClickListener {
            if (activityMainBinding.inputText.text.isNullOrEmpty()) {
                classifierHelper.classify(getString(R.string.default_edit_text))
            }
            else {
                classifierHelper.classify(activityMainBinding.inputText.text.toString())
            }
        }

        activityMainBinding.results.adapter = adapter
        initBottomeSheetControls()
    }

    private fun initBottomeSheetControls() {
        val behavior = BottomSheetBehavior.from(activityMainBinding.bottomSheetLayout.bottomSheetLayout)
        behavior.state = BottomSheetBehavior.STATE_EXPANDED
        // When clicked, change the underlying hardware used for inference. Current options are CPU
        // and NNAPI. GPU is another available option, but when using this option you will need
        // to initialize the classifier on the thread that does the classifying.
        activityMainBinding.bottomSheetLayout.spinnerDelegate.onItemSelectedListener =
            object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(
                    parent: AdapterView<*>?,
                    view: View?,
                    position: Int,
                    id: Long
                ) {
                    classifierHelper.currentDelegate = position
                    classifierHelper.initClassifier()
                }

                override fun onNothingSelected(p0: AdapterView<*>?) {
                    /* no op */
                }
            }

        activityMainBinding.bottomSheetLayout.spinnerDelegate.setSelection(
            0,
            false
        )

        // Allows the user to switch between the classification models that are available.
        activityMainBinding.bottomSheetLayout.modelSelector.setOnCheckedChangeListener(
            object : RadioGroup.OnCheckedChangeListener {
                override fun onCheckedChanged(group: RadioGroup?, checkedId: Int) {
                    when (checkedId) {
                        R.id.wordvec -> {
                            classifierHelper.currentModel = TextClassificationHelper.WORD_VEC
                            classifierHelper.initClassifier()
                        }
                        R.id.mobilebert -> {
                            classifierHelper.currentModel = TextClassificationHelper.MOBILEBERT
                            classifierHelper.initClassifier()
                        }
                    }
                }
            }
        )
    }

    override fun onBackPressed() {
        if (Build.VERSION.SDK_INT == Build.VERSION_CODES.Q) {
            // Workaround for Android Q memory leak issue in IRequestFinishCallback$Stub.
            // (https://issuetracker.google.com/issues/139738913)
            finishAfterTransition()
        } else {
            super.onBackPressed()
        }
    }
}