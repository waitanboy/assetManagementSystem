package com.assetmanagement.backend.controller;

import com.assetmanagement.backend.entity.Department;
import com.assetmanagement.backend.mapper.DepartmentMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/departments")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class DepartmentController {

    private final DepartmentMapper departmentMapper;

    @GetMapping
    @PreAuthorize("permitAll()") // Allow all authenticated users to see the list (for dropdowns)
    public ResponseEntity<List<Department>> getAllDepartments() {
        return ResponseEntity.ok(departmentMapper.findAll());
    }

    @PostMapping
    public ResponseEntity<Department> createDepartment(@RequestBody Department department) {
        departmentMapper.insert(department);
        return new ResponseEntity<>(department, HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Void> updateDepartment(@PathVariable Long id, @RequestBody Department department) {
        department.setId(id);
        departmentMapper.update(department);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteDepartment(@PathVariable Long id) {
        departmentMapper.delete(id);
        return ResponseEntity.ok().build();
    }
}
