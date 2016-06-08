package com.cyg.learn;

import java.security.Provider;

/**
 * Created by wm on 16/6/6.
 */
public class TestProvider extends Provider {
    /**
     * Constructs a new instance of {@code Provider} with its name, version and
     * description.
     *
     * @param name    the name of the provider.
     * @param version the version of the provider.
     * @param info
     */
    protected TestProvider(String name, double version, String info) {
        super(name, version, info);
    }
}
