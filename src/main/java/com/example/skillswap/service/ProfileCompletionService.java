package com.example.skillswap.service;

import com.example.skillswap.entity.Profil;
import com.example.skillswap.entity.User;

import java.security.Principal;

public interface ProfileCompletionService {

    String REQUIRED_REDIRECT_PATH = "/profile/complete?required";

    boolean calculateProfileCompletion(Profil profile);

    boolean refreshProfileCompletion(User user, Profil profile);

    boolean isProfileCompleted(Principal principal);

    boolean isProfileCompleted(User user);

    String getRequiredRedirectView();
}
