package com.xsb.sdk.reporter


import com.xsb.sdk.model.ANRData
import com.xsb.sdk.model.FluencyData

interface IReporter {
    fun reportFluency(data: FluencyData)
    fun reportANR(data: ANRData)
}

