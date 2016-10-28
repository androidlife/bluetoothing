package com.wordpress.laaptu.bluetooth.test.refactor.bluetooth;

import com.wordpress.laaptu.bluetooth.test.refactor.base.ClientServerProvider;
import com.wordpress.laaptu.bluetooth.test.refactor.base.Provider;

/**
 * Created by laaptu on 10/28/16.
 */

public class BTProvider implements Provider {
    public BTProvider(UIProvider uiProvider) {
        BTClientServerProvider provider = null;
        uiProvider.setIncomingActions(provider);

    }

    @Override
    public void start() {

    }

    @Override
    public void stop() {

    }
}
