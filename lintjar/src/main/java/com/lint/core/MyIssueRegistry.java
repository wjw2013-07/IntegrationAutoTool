package com.lint.core;

import com.android.tools.lint.client.api.IssueRegistry;
import com.android.tools.lint.detector.api.ApiKt;
import com.android.tools.lint.detector.api.Issue;

import java.util.Arrays;
import java.util.List;

public class MyIssueRegistry extends IssueRegistry {

    @Override
    public List<Issue> getIssues() {
        System.out.println("====api=" + getApi() + ",minApi=" + getMinApi()+",CurrentApi="+ ApiKt.CURRENT_API);

        return Arrays.asList(LogDetector.ISSUE,
                NewThreadDetector.ISSUE,
                HashMapDetector.USE_SPARSE_ARRAY);
    }

    @Override
    public int getApi() {
        return ApiKt.CURRENT_API;
    }

    @Override
    public int getMinApi() {  //兼容3.1
        return 1;
    }
}
