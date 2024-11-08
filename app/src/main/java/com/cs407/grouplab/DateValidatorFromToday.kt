package com.cs407.grouplab

import android.os.Parcel
import com.google.android.material.datepicker.CalendarConstraints
import java.util.Calendar

// Custom DateValidator that allows only dates from today onwards
class DateValidatorFromToday : CalendarConstraints.DateValidator {
    override fun isValid(date: Long): Boolean {
        val today = Calendar.getInstance()
        today.set(Calendar.HOUR_OF_DAY, 0)
        today.set(Calendar.MINUTE, 0)
        today.set(Calendar.SECOND, 0)
        today.set(Calendar.MILLISECOND, 0)
        return date >= today.timeInMillis
    }

    override fun writeToParcel(dest: Parcel, flags: Int) {}
    override fun describeContents(): Int = 0

    companion object CREATOR : android.os.Parcelable.Creator<DateValidatorFromToday> {
        override fun createFromParcel(source: android.os.Parcel): DateValidatorFromToday {
            return DateValidatorFromToday()
        }

        override fun newArray(size: Int): Array<DateValidatorFromToday?> {
            return arrayOfNulls(size)
        }
    }
}