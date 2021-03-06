package ch.rmy.android.http_shortcuts.variables.types

import android.app.DatePickerDialog
import android.content.Context
import android.content.DialogInterface
import ch.rmy.android.http_shortcuts.R
import ch.rmy.android.http_shortcuts.realm.Controller
import ch.rmy.android.http_shortcuts.realm.models.Variable
import ch.rmy.android.http_shortcuts.utils.showIfPossible
import org.jdeferred.Deferred
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*


internal class DateType : BaseVariableType(), AsyncVariableType {

    override val hasTitle = false

    override fun createDialog(context: Context, controller: Controller, variable: Variable, deferredValue: Deferred<String, Unit, Unit>): () -> Unit {
        val calendar = getInitialDate(variable.value)
        val datePicker = DatePickerDialog(context, null, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH))
        datePicker.setButton(DialogInterface.BUTTON_POSITIVE, context.getString(R.string.button_ok),
                { _, _ ->
                    val newDate = Calendar.getInstance()
                    val day = datePicker.getDatePicker().getDayOfMonth()
                    val month = datePicker.getDatePicker().getMonth()
                    val year = datePicker.getDatePicker().getYear()
                    newDate.set(year, month, day)
                    if (deferredValue.isPending) {
                        try {
                            val dateFormat = SimpleDateFormat(variable.dataForType[KEY_FORMAT] ?: DEFAULT_FORMAT, Locale.US)
                            deferredValue.resolve(dateFormat.format(newDate.time))
                            if (variable.rememberValue) {
                                controller.setVariableValue(variable.id, DATE_FORMAT.format(newDate.time))
                            }
                        } catch (e: Exception) {
                            deferredValue.reject(Unit)
                        }
                    }
                })
        datePicker.setCancelable(true)
        datePicker.setCanceledOnTouchOutside(true)
        return {
            datePicker.showIfPossible()
            datePicker.setOnDismissListener {
                if (deferredValue.isPending) {
                    deferredValue.reject(Unit)
                }
            }
        }
    }

    private fun getInitialDate(previousValue: String?): Calendar {
        val calendar = Calendar.getInstance()
        if (previousValue != null) {
            try {
                calendar.time = DATE_FORMAT.parse(previousValue)
            } catch (e: ParseException) {
            }
        }
        return calendar
    }

    override fun createEditorFragment() = DateEditorFragment()

    companion object {

        const val KEY_FORMAT = "format"
        const val DEFAULT_FORMAT = "yyyy-MM-dd"

        private val DATE_FORMAT = SimpleDateFormat("yyyy-MM-dd", Locale.US)

    }

}
