/*
 * Copyright 2021 SkyAPM
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package org.skyapm.e2e.controller;

import lombok.RequiredArgsConstructor;
import org.apache.skywalking.apm.toolkit.trace.TraceContext;
import org.skyapm.e2e.User;
import org.skyapm.e2e.UserRepo;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.slf4j.LoggerFactory;

import java.util.Random;

@RestController
@RequiredArgsConstructor
@SuppressWarnings("SameReturnValue")
public class UserController {
    private static final org.slf4j.Logger LOGBACK_LOGGER = LoggerFactory.getLogger(UserController.class);

    private final UserRepo userRepo;
    private final int sleepMin = 500;
    private final int sleepMax = 1000;

    @PostMapping("/info")
    public String info() throws InterruptedException {
        Thread.sleep(randomSleepLong(sleepMin, sleepMax));
        LOGBACK_LOGGER.info("logback message==> now: {}", System.currentTimeMillis());
        return "whatever";
    }

    @PostMapping("/users")
    public User createAuthor() throws InterruptedException {
        Thread.sleep(randomSleepLong(sleepMin, sleepMax));
        return userRepo.save(User.builder().name("").build());
    }

    @PostMapping("/correlation")
    public String correlation() throws InterruptedException {
        Thread.sleep(randomSleepLong(sleepMin, sleepMax));
        TraceContext.putCorrelation("PROVIDER_KEY", "provider");
        return TraceContext.getCorrelation("CONSUMER_KEY").orElse("") + "_"
            + TraceContext.getCorrelation("MIDDLE_KEY").orElse("") + "_"
            + TraceContext.getCorrelation("PROVIDER_KEY").orElse("");
    }

    private long randomSleepLong(int min, int max) {
        Random rand = new Random();
        int randomNumber = rand.nextInt((max - min) + 1) + min;
        return randomNumber;
    }
}
