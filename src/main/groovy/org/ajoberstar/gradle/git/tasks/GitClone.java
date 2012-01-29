/* Copyright 2012 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.ajoberstar.gradle.git.tasks;

import groovy.lang.Closure;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;

import org.ajoberstar.gradle.util.ObjectUtil;
import org.eclipse.jgit.api.CloneCommand;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.transport.CredentialsProvider;
import org.eclipse.jgit.transport.UsernamePasswordCredentialsProvider;
import org.gradle.api.DefaultTask;
import org.gradle.api.artifacts.repositories.AuthenticationSupported;
import org.gradle.api.artifacts.repositories.PasswordCredentials;
import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.OutputDirectory;
import org.gradle.api.tasks.TaskAction;
import org.gradle.util.ConfigureUtil;

/**
 * 
 * @since 0.1.0
 */
public class GitClone extends DefaultTask implements AuthenticationSupported {
	private PasswordCredentials credentials = null;
	private Object uri = null;
	private Object remote = null;
	private boolean bare = false;
	private boolean checkout = true;
	private Object branch = null;
	private Collection<Object> branchesToClone = null;
	private boolean cloneAllBranches = true;
	private Object destinationPath = null;
	
	@TaskAction
	public void cloneRepo() {
		CloneCommand cmd = Git.cloneRepository();
		cmd.setCredentialsProvider(getCredentialsProvider());
		cmd.setURI(getUri().toString());
		cmd.setRemote(getRemote());
		cmd.setBare(getBare());
		cmd.setNoCheckout(!getCheckout());
		cmd.setBranch("refs/heads/" + getBranch());
		cmd.setBranchesToClone(getBranchesToClone());
		cmd.setCloneAllBranches(getCloneAllBranches());
		cmd.setDirectory(getDestinationDir());
		cmd.call();
		//TODO add progress monitor to log progress to Gradle status bar
		//TODO add support for credentials
	}

	@Input
	public PasswordCredentials getCredentials() {
		return credentials;
	}
	
	@SuppressWarnings("rawtypes")
	public void credentials(Closure closure) {
		ConfigureUtil.configure(closure, getCredentials());
	}
	
	public void setCredentials(PasswordCredentials credentials) {
		this.credentials = credentials;
	}
	
	private CredentialsProvider getCredentialsProvider() {
		if (getCredentials() == null
			|| ((getCredentials().getUsername() == null || getCredentials().getUsername().trim() == "")
			&& (getCredentials().getPassword() == null || getCredentials().getPassword().trim() == ""))) {
			return null;
		}
		return new UsernamePasswordCredentialsProvider(getCredentials().getUsername(), getCredentials().getPassword());
	}
	
	@Input
	public String getUri() {
		return ObjectUtil.unpackString(uri);
	}
	
	public void setUri(Object uri) {
		this.uri = uri;
	}
	
	@Input
	public String getRemote() {
		return remote == null ? "origin" : ObjectUtil.unpackString(remote);
	}
	
	public void setRemote(Object remote) {
		this.remote = remote;
	}
	
	@Input
	public boolean getBare() {
		return bare;
	}
	
	public void setBare(boolean bare) {
		this.bare = bare;
	}
	
	@Input
	public boolean getCheckout() {
		return checkout;
	}
	
	public void setCheckout(boolean checkout) {
		this.checkout = checkout;
	}
	
	@Input
	public String getBranch() {
		return branch == null ? "master" : ObjectUtil.unpackString(branch);
	}
	
	public void setBranch(Object branch) {
		this.branch = branch;
	}
	
	@OutputDirectory
	public File getDestinationDir() {
		return getProject().file(destinationPath);
	}
	
	public void setDestinationPath(Object destinationPath) {
		this.destinationPath = destinationPath;
	}
	
	@Input
	public Collection<String> getBranchesToClone() {
		if (branchesToClone == null) {
			return Arrays.asList(getBranch());
		}
		Collection<String> branches = new HashSet<String>();
		for (Object branch : branchesToClone) {
			branches.add(ObjectUtil.unpackString(branch));
		}
		return branches;
	}
	
	public void branchesToClone(Object... branches) {
		if (branchesToClone == null) {
			this.branchesToClone = new ArrayList<Object>();
		}
		Collections.addAll(branchesToClone, branches);
	}
	
	@SuppressWarnings("unchecked")
	public void setBranchesToClone(Collection<? extends Object> branchesToClone) {
		this.branchesToClone = (Collection<Object>) branchesToClone;
		setCloneAllBranches(false);
	}
	
	@Input
	public boolean getCloneAllBranches() {
		return cloneAllBranches;
	}
	
	public void setCloneAllBranches(boolean cloneAllBranches) {
		this.cloneAllBranches = cloneAllBranches;
	}
}