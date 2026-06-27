package com.rukshan.ranaswanu.controller;

import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/lang")
@AllArgsConstructor
public class LanguageController {


    @GetMapping("/{lang}")
    public String assignLanguage(@PathVariable String lang){
        return lang;
    }
}