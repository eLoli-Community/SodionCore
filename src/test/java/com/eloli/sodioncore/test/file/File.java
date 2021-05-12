package com.eloli.sodioncore.test.file;

import com.eloli.sodioncore.file.BaseFileService;
import org.junit.jupiter.api.Test;

public class File {
    @Test
    public void save() throws Exception {
        BaseFileService baseFileService = new BaseFileService("./run");
        baseFileService.saveResource("probeResource", true);
    }
}
