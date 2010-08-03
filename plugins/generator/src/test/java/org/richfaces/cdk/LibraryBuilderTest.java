/**
 *
 */
package org.richfaces.cdk;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.maven.model.FileSet;
import org.apache.maven.plugin.MojoExecutionException;
import org.codehaus.plexus.util.DirectoryScanner;
import org.junit.Ignore;
import org.junit.Test;

/**
 * @author asmirnov
 * 
 */
public class LibraryBuilderTest extends CdkTestBase {
    private static final String[] JAVA_INCLUDES = new String[] { "**/*.java" };
    private static final String MAIN_CONFIG = "src/main/config";
    private static final String MAIN_TEMPLATES = "src/main/templates";
    private static final String PROJECT_BASE = "E:\\_richfaces\\svn\\richfaces_root\\ui\\components\\core\\";
    private static final String[] STRINGS_ARRAY = new String[0];
    private static final String XML_INCLUDES = "**/*.xml";

    protected File outputJavaDirectory = new File(PROJECT_BASE + "target/generated-sources/main/java");
    protected File outputResourcesDirectory = new File(PROJECT_BASE + "target/generated-sources/main/resources");
    protected File outputTestDirectory = new File(PROJECT_BASE + "target/generated-sources/test/java");

    protected File outputTestResourcesDirectory = new File(PROJECT_BASE + "target/generated-sources/test/resources");

    /**
     * <p class="changed_added_4_0">
     * This utility method sets output directory for particular type. I such directory does not exist, it is created.
     * </p>
     * 
     * @param generator
     * @param directory
     * @param type
     */
    private static void setOutput(Generator generator, File directory, Outputs type) {
        if (!directory.exists()) {
            directory.mkdirs();
        }
    
        generator.addOutputFolder(type, directory);
    }

    @Test
    @Ignore
    public void createInstance() throws Exception {
        List<String> compileSourceRoots = Arrays.asList("");

        Generator generator = new Generator();
        generator.setLoader(createClassLoader());
        // Set source folders.
        ArrayList<File> folders = new ArrayList<File>(compileSourceRoots.size());

        for (String sourceFolder : compileSourceRoots) {
            File folder = new File(sourceFolder);

            if (folder.exists() && folder.isDirectory()) {
                folders.add(folder);
            }
        }

        generator.addSources(Sources.JAVA_SOURCES, findJavaFiles(), folders);
        // TODO - detect templates and configs directories.
        generator.addSources(Sources.RENDERER_TEMPLATES, findTemplateFiles(), null);
        generator.addSources(Sources.FACES_CONFIGS, findFacesConfigFiles(), null);

        // Setup output folders.
        setOutput(generator, outputJavaDirectory, Outputs.JAVA_CLASSES);
        setOutput(generator, outputResourcesDirectory, Outputs.RESOURCES);
        setOutput(generator, outputTestDirectory, Outputs.TEST_JAVA_CLASSES);
        setOutput(generator, outputTestResourcesDirectory, Outputs.TEST_RESOURCES);

        // configure CDK workers.
        // setupPlugins(generator);

        try {

            // Build JSF library.
            // LibraryBuilder builder = LibraryBuilder.createInstance(context);
            generator.init();
            generator.execute();
        } catch (CdkException e) {
            throw new MojoExecutionException("CDK build error", e);
        }
    }

    /**
     * <p class="changed_added_4_0">
     * This method checks library configuration and sets default values if necessary.
     * </p>
     */
    protected void checkLibraryConfig() {

        // TODO Auto-generated method stub
    }

    @Override
    protected CdkClassLoader createClassLoader() {
        return new CdkClassLoader(this.getClass().getClassLoader());
    }

    /**
     * Skan Array of filesets for selected resources.
     * 
     * @param filesets
     * @return
     * @throws MojoExecutionException
     */
    @SuppressWarnings("unchecked")
    protected Collection<File> doScan(FileSet[] filesets) throws MojoExecutionException {
        List<File> files = new ArrayList<File>();

        if (null != filesets) {
            for (FileSet fileSet : filesets) {
                String[] includes = (String[]) fileSet.getIncludes().toArray(STRINGS_ARRAY);
                String[] excludes = (String[]) fileSet.getExcludes().toArray(STRINGS_ARRAY);
                File fileSetDirectory = resolveRelativePath(new File(fileSet.getDirectory()));
                String[] scan = doScan(includes, excludes, fileSetDirectory);

                for (String filename : scan) {
                    files.add(resolveRelativePath(new File(fileSetDirectory, filename)));
                }
            }
        }

        return files;
    }

    protected String[] doScan(String[] includes, String[] excludes, File rootFolder) throws MojoExecutionException {
        try {
            DirectoryScanner directoryScanner = new DirectoryScanner();

            directoryScanner.setFollowSymlinks(true);
            directoryScanner.setBasedir(rootFolder);
            directoryScanner.setExcludes(excludes);
            directoryScanner.setIncludes(includes);
            directoryScanner.addDefaultExcludes();
            directoryScanner.scan();

            return directoryScanner.getIncludedFiles();
        } catch (IllegalStateException e) {
            throw new MojoExecutionException("Error scanning source root: \'" + rootFolder + "\'", e);
        }
    }

    private Iterable<File> findFacesConfigFiles() throws MojoExecutionException {
        FileSet[] facesConfigs = null;
        File defaultDirectory = resolveRelativePath(new File(MAIN_CONFIG));

        if (defaultDirectory.exists() && defaultDirectory.isDirectory()) {
            FileSet fileSet = new FileSet();

            fileSet.setDirectory(MAIN_CONFIG);
            fileSet.addInclude(XML_INCLUDES);
            facesConfigs = new FileSet[] { fileSet };
        }

        return doScan(facesConfigs);
    }

    private Iterable<File> findJavaFiles() throws MojoExecutionException {
        Set<File> javaSources = new HashSet<File>();
        String[] includes = JAVA_INCLUDES;
        String[] compileSourceRoots = new String[] { PROJECT_BASE + "\\src\\main\\java" };
        for (String compileRoot : compileSourceRoots) {
            File rootFolder = new File(compileRoot);
            String[] sources = doScan(includes, null, rootFolder);

            for (String src : sources) {
                javaSources.add(new File(rootFolder, src));
            }
        }

        return javaSources;
    }

    private Iterable<File> findTemplateFiles() throws MojoExecutionException {
        FileSet[] templates = null;
        File defaultDirectory = resolveRelativePath(new File(MAIN_TEMPLATES));

        if (defaultDirectory.exists() && defaultDirectory.isDirectory()) {
            FileSet fileSet = new FileSet();

            fileSet.setDirectory(MAIN_TEMPLATES);
            fileSet.addInclude(XML_INCLUDES);
            templates = new FileSet[] { fileSet };
        }

        return doScan(templates);
    }

    private File resolveRelativePath(File file) {
        File result = file;
        if (!result.isAbsolute()) {
            result = new File(PROJECT_BASE, result.getPath());
        }

        return result;
    }
}
