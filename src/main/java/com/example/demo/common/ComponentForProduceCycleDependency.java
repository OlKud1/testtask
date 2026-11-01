package com.example.demo.common;

import com.example.demo.service.ParcelServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ComponentForProduceCycleDependency {
    private final ParcelServiceImpl parcelService;

    public void doNothing() {
        parcelService.methodForTrick();
    }
}
