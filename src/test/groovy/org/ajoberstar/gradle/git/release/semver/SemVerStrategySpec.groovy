/*
 * Copyright 2012-2014 the original author or authors.
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
package org.ajoberstar.gradle.git.release.semver

import spock.lang.Specification

import org.gradle.api.Project
import org.ajoberstar.grgit.Grgit
import org.ajoberstar.grgit.Status
import org.ajoberstar.grgit.Branch
import org.ajoberstar.grgit.BranchStatus
import org.ajoberstar.grgit.service.BranchService

class SemVerStrategySpec extends Specification {
	Project project = GroovyMock()
	Grgit grgit = GroovyMock()

	def 'selector returns false if stage is not set to valid value'() {
		given:
		def strategy = new SemVerStrategy(stages: ['one', 'two'] as SortedSet)
		mockStage(stageProp)
		expect:
		!strategy.selector(project, grgit)
		where:
		stageProp << [null, 'test']
	}


	def 'selector returns false if repo is dirty and not allowed to be'() {
		given:
		def strategy = new SemVerStrategy(stages: ['one'] as SortedSet, allowDirtyRepo: false, allowBranchBehind: false)
		mockStage('one')
		mockRepoClean(false)
		expect:
		!strategy.selector(project, grgit)
	}

	def 'selector returns false if branch behind its tracked branch and not allowed to be'() {
		given:
		def strategy = new SemVerStrategy(stages: ['one'] as SortedSet, allowDirtyRepo: false, allowBranchBehind: false)
		mockStage('one')
		mockRepoClean(true)
		mockBranchService(2)
		expect:
		!strategy.selector(project, grgit)
	}

	def 'selector returns true if repo is dirty and allowed and other criteria met'() {
		given:
		def strategy = new SemVerStrategy(stages: ['one'] as SortedSet, allowDirtyRepo: true, allowBranchBehind: false)
		mockStage('one')
		mockRepoClean(false)
		mockBranchService(0)
		expect:
		strategy.selector(project, grgit)
	}

	def 'selector returns true if branch is behind and allowed to be and other criteria met'() {
		given:
		def strategy = new SemVerStrategy(stages: ['one'] as SortedSet, allowDirtyRepo: false, allowBranchBehind: true)
		mockStage('one')
		mockRepoClean(true)
		mockBranchService(2)
		expect:
		strategy.selector(project, grgit)
	}

	def 'selector returns true if all criteria met'() {
		given:
		def strategy = new SemVerStrategy(stages: ['one'] as SortedSet, allowDirtyRepo: false, allowBranchBehind: false)
		mockStage('one')
		mockRepoClean(true)
		mockBranchService(0)
		expect:
		strategy.selector(project, grgit)
	}

	private def mockStage(String stageProp) {
		(0..1) * project.hasProperty('release.stage') >> (stageProp as boolean)
		(0..1) * project.property('release.stage') >> stageProp
		0 * project._
	}

	private def mockRepoClean(boolean isClean) {
		Status status = GroovyMock()
		(0..1) * status.clean >> isClean
		(0..1) * grgit.status() >> status
		0 * status._
	}

	private def mockBranchService(int behindCount) {
		BranchService branchService = GroovyMock()
		(0..1) * branchService.status(_) >> new BranchStatus(behindCount: behindCount)
		(0..1) * branchService.current >> new Branch(fullName: 'refs/heads/master')
		(0..2) * grgit.getBranch() >> branchService
		0 * branchService._
		0 * grgit._
	}
}