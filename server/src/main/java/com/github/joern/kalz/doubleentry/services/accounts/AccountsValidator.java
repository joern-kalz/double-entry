package com.github.joern.kalz.doubleentry.services.accounts;

import com.github.joern.kalz.doubleentry.models.Account;
import org.springframework.stereotype.Service;

@Service
public class AccountsValidator {

    private static final int MAXIMUM_ACCOUNT_HIERARCHY_DEPTH = 100;

    public enum Result {OK, CYCLIC_PARENT_CHILD_RELATIONSHIP, MAXIMUM_HIERARCHY_DEPTH_EXCEEDED}

    public Result validate(Account account) {
        Account currentPosition = account.getParent();

        for (int i = 0; i < MAXIMUM_ACCOUNT_HIERARCHY_DEPTH && currentPosition != null; i++) {
            if (currentPosition.getId().equals(account.getId())) {
                return Result.CYCLIC_PARENT_CHILD_RELATIONSHIP;
            }

            currentPosition = currentPosition.getParent();
        }

        if (currentPosition != null) {
            return Result.MAXIMUM_HIERARCHY_DEPTH_EXCEEDED;
        }

        return Result.OK;
    }

}
