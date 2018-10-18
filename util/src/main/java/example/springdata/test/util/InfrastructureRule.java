/*
 * Copyright 2018 the original author or authors.
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
package example.springdata.test.util;

import lombok.Value;

import java.util.function.Supplier;

import org.junit.AssumptionViolatedException;
import org.junit.rules.ExternalResource;
import org.junit.rules.TestRule;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.data.util.Lazy;

/**
 * @author Jens Schauder
 */
public class InfrastructureRule extends ExternalResource implements DisposableBean {

	private final Supplier<ResourceInfo> cachedResourceInfo;

	static public InfrastructureRule.ResourceInfo buildInfoForFirstWorkingSource(
			Supplier<ResourceInfo>... possibleSources) {

		Exception lastException = null;
		for (Supplier<ResourceInfo> source : possibleSources) {

			ResourceInfo info = source.get();
			if (info.isValid()) {
				return info;
			}

			lastException = info.getException();
		}

		return new ResourceInfo(false, null, lastException, ()->{});
	}

	public InfrastructureRule(Supplier<ResourceInfo>... possibleSources) {
		this(() -> buildInfoForFirstWorkingSource(possibleSources));
	}

	public InfrastructureRule(Supplier<ResourceInfo> resourceInfoSupplier) {

		this.cachedResourceInfo = Lazy.of(resourceInfoSupplier);
	}

	@Override
	protected void before() throws Throwable {

		if (! cachedResourceInfo.get().isValid()) {
			throw new AssumptionViolatedException("No Solr Instance available");
		}
	}

	public String getInfo() {
		return cachedResourceInfo.get().getInfo();
	}

	@Override
	public void destroy() throws Exception {
		cachedResourceInfo.get().destroy.run();
	}

	@Value
	public static class ResourceInfo {

		boolean valid;
		String info;
		Exception exception;

		Runnable destroy;
	}
}
