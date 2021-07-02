package com.dmdev.junit.extension;

import org.junit.jupiter.api.extension.AfterAllCallback;
import org.junit.jupiter.api.extension.BeforeAllCallback;
import org.junit.jupiter.api.extension.ExtensionContext;

public class GlobalExtension implements BeforeAllCallback, AfterAllCallback {

    @Override
    public void beforeAll(ExtensionContext extensionContext) {
        System.out.println("Before all extension ::)");
    }

    @Override
    public void afterAll(ExtensionContext extensionContext) throws Exception {
        System.out.println("After all extension ::)");
    }
}
