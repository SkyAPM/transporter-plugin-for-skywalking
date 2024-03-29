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

package test.skyapm.e2e.profile;

import java.util.concurrent.TimeUnit;
import lombok.RequiredArgsConstructor;
import org.skyapm.e2e.User;
import org.skyapm.e2e.UserRepo;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class ProfileController {
    private final UserRepo userRepo;

    @PostMapping("/profile/{name}")
    public User createAuthor(@RequestBody final CreateUser createUser) throws InterruptedException {
        final User user = userRepo.save(createUser.toUser());
        if (createUser.isEnableProfiling()) {
            TimeUnit.MILLISECONDS.sleep(6200);
        }
        return user;
    }
}
