package com.example;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileVisitResult;
import java.nio.file.FileVisitor;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Collection;
import java.util.List;
import java.util.regex.Pattern;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.springframework.cloud.contract.spec.Contract;
import org.springframework.cloud.contract.verifier.util.ContractVerifierDslConverter;

@Mojo(name = "contract-docs-generator", defaultPhase = LifecyclePhase.PROCESS_TEST_RESOURCES)
public class GenerateAdocsFromContractTests extends AbstractMojo {
	
	@Parameter(defaultValue = "${project.testClasspathElements}", readonly = true, required = true)
	private List<String> compilePath;
	
	@Parameter(property = "outputDirPath", defaultValue = "target/generated-snippets")
	private String outputDirPath;
	
	@Parameter(property = "outputFileName", defaultValue = "contracts.adoc")
	private String outputFileName;
	
	private static String header = "= Application Contracts\n" + "\n"
			+ "In the following document you'll be able to see all the contracts that are present for this application.\n"
			+ "\n" + "== Contracts\n";
	
	@Override
	public void execute() throws MojoExecutionException, MojoFailureException {
		if (!compilePath.isEmpty()) {
			try {
				generateDocs(compilePath);
			} catch (IOException e) {
				getLog().error("There was error during generating docs!", e);
			}
		}
	}
	
	public void generateDocs(List<String> paths) throws IOException {
		final StringBuilder stringBuilder = new StringBuilder();
		stringBuilder.append(header);
		
		paths.forEach(path -> {
			if (path != null) {
				getLog().info("Processing path: " + path);
				final Path rootDir = new File(path).toPath();
				
				try {
					Files.walkFileTree(rootDir, new FileVisitor<Path>() {
						private Pattern pattern = Pattern.compile("^.*groovy$");
						
						@Override
						public FileVisitResult preVisitDirectory(Path path, BasicFileAttributes atts)
								throws IOException {
							return FileVisitResult.CONTINUE;
						}
						
						@Override
						public FileVisitResult visitFile(Path path, BasicFileAttributes mainAtts)
								throws IOException {
							getLog().info("File with path: " + path.getFileName()
									.toString());
							boolean matches = this.pattern.matcher(path.toString())
									.matches();
							if (matches) {
								appendContract(stringBuilder, path);
							}
							return FileVisitResult.CONTINUE;
						}
						
						@Override
						public FileVisitResult postVisitDirectory(Path path, IOException exc)
								throws IOException {
							return FileVisitResult.CONTINUE;
						}
						
						@Override
						public FileVisitResult visitFileFailed(Path path, IOException exc)
								throws IOException {
							// If the root directory has failed it makes no sense to continue
							return path.equals(rootDir) ? FileVisitResult.TERMINATE : FileVisitResult.CONTINUE;
						}
					});
				} catch (IOException e) {
					getLog().info("There was error during generating docs! Generating docs from path: " + path + " aborted!", e);
				}
			}
		});
		
		String outputAdoc = stringBuilder.toString();
		File outputDir = new File(this.outputDirPath);
		outputDir.mkdirs();
		File outputFile = new File(outputDir, this.outputFileName);
		if (outputFile.exists()) {
			outputFile.delete();
		}
		if (outputFile.createNewFile()) {
			Files.write(outputFile.toPath(), outputAdoc.getBytes());
		}
	}
	
	static StringBuilder appendContract(final StringBuilder stringBuilder, Path path)
			throws IOException {
		Collection<Contract> contracts = ContractVerifierDslConverter.convertAsCollection(path.getParent()
				.toFile(), path.toFile());
		// TODO: fit it to our preferences
		contracts.forEach(contract -> {
			stringBuilder.append("### ")
					.append(path.getFileName()
							.toString())
					.append("\n\n")
					.append(contract.getDescription())
					.append("\n\n")
					.append("#### Contract structure")
					.append("\n\n")
					.append("[source,java,indent=0]")
					.append("\n")
					.append("----")
					.append("\n")
					.append(fileAsString(path))
					.append("\n")
					.append("----")
					.append("\n\n");
		});
		return stringBuilder;
	}
	
	static String fileAsString(Path path) {
		try {
			byte[] encoded = Files.readAllBytes(path);
			return new String(encoded, StandardCharsets.UTF_8);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}
	
}
