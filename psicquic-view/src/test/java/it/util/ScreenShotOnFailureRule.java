/**
 * Copyright 2012 The European Bioinformatics Institute, and others.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package it.util;

import org.junit.rules.TestRule;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

public class ScreenShotOnFailureRule implements TestRule {

    private WebDriver driver;

    public ScreenShotOnFailureRule() {
    }

    public void setDriver(WebDriver driver) {
        this.driver = driver;
    }

    @Override
    public Statement apply(final Statement base, final Description description) {
        return new Statement() {
            @Override
            public void evaluate() throws Throwable {
                try {
                    base.evaluate();
                } catch (Throwable t) {
                    takeScreenshot(description.getClassName() + "#"
                            + description.getMethodName());

//                    System.out.println("--------- Page Source ----------------");
//                    System.out.println(driver.getPageSource());
//                    System.out.println("--------------------------------------");

                    throw t;
                }
            }
        };
    }

    protected void saveFile(String name, byte [] data) throws IOException {
        String basedir = System.getProperty("basedir");
        if(basedir != null) {
            basedir += "/target/";
        } else {
            basedir = "target/";
        }
        basedir += "failsafe-reports";

        File dir = new File(basedir);
        dir.mkdirs();
        File f = new File(dir, name);
        FileOutputStream fs = new FileOutputStream(f);
        fs.write(data);
        fs.close();
    }

    protected void takeScreenshot(String baseName) {
        try {
            byte[] data = ((TakesScreenshot) driver).getScreenshotAs(OutputType.BYTES);
            saveFile(baseName + ".png", data);
        } catch(Throwable t) {
            System.err.print("Cannot save screenshot for failed test: "+baseName+".png");
        }
    }
}
