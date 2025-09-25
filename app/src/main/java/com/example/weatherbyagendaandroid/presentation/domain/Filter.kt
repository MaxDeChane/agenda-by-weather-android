package com.example.weatherbyagendaandroid.presentation.domain

interface Filter<T> {
    fun filter(elementToFilter: T): Boolean
}