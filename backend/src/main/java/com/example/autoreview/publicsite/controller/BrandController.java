package com.example.autoreview.publicsite.controller;

import com.example.autoreview.publicsite.dto.response.BrandDto;
import com.example.autoreview.publicsite.service.BrandService;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/brands")
public class BrandController {

    private final BrandService brandService;

    public BrandController(BrandService brandService) {
        this.brandService = brandService;
    }

    @GetMapping("/featured")
    public ResponseEntity<List<BrandDto>> featured() {
        return ResponseEntity.ok(brandService.featured());
    }
}
