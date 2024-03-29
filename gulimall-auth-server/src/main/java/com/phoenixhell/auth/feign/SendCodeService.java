package com.phoenixhell.auth.feign;

import com.phoenixhell.common.utils.R;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient("gulimall-third-party")
public interface SendCodeService {
    @GetMapping("/sms/send")
    R sendCode(@RequestParam("phone") String phone, @RequestParam("code") String code);
}
