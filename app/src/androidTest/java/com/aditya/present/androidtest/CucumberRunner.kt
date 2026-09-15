package com.aditya.present.androidtest

import io.cucumber.junit.CucumberOptions

@CucumberOptions(
    features = ["features"],
    glue = ["com.aditya.present.androidtest.steps"],
    plugin = ["pretty"],
    monochrome = true,
)
class CucumberRunner
