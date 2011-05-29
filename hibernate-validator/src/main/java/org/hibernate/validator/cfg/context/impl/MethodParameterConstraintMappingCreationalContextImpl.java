/*
 * JBoss, Home of Professional Open Source
 * Copyright 2010, Red Hat, Inc. and/or its affiliates, and individual contributors
 * by the @authors tag. See the copyright.txt in the distribution for a
 * full listing of individual contributors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.hibernate.validator.cfg.context.impl;

import static java.lang.annotation.ElementType.PARAMETER;

import java.lang.reflect.Method;
import java.util.Arrays;

import org.hibernate.validator.cfg.ConfiguredConstraint;
import org.hibernate.validator.cfg.ConstraintDef;
import org.hibernate.validator.cfg.ConstraintMapping;
import org.hibernate.validator.cfg.MethodCascadeDef;
import org.hibernate.validator.cfg.context.MethodParameterConstraintMappingCreationalContext;
import org.hibernate.validator.cfg.context.MethodReturnValueConstraintMappingCreationalContext;
import org.hibernate.validator.util.Contracts;
import org.hibernate.validator.util.ReflectionHelper;

/**
 * Constraint mapping creational context which allows to configure the constraints for one method parameter.
 *
 * @author Hardy Ferentschik
 * @author Gunnar Morling
 * @author Kevin Pollet - SERLI - (kevin.pollet@serli.com)
 */
public final class MethodParameterConstraintMappingCreationalContextImpl extends ConstraintMappingCreationalContextImplBase
		implements MethodParameterConstraintMappingCreationalContext {

	private final String methodName;
	private final Class<?>[] parameterTypes;
	private final int parameterIndex;
	private final Method method;

	public MethodParameterConstraintMappingCreationalContextImpl(Class<?> beanClass, String methodName, Class<?>[] parameterTypes, int parameterIndex, ConstraintMapping mapping) {

		super( beanClass, mapping );

		this.methodName = methodName;
		this.parameterTypes = parameterTypes;
		this.parameterIndex = parameterIndex;
		
		this.method = ReflectionHelper.getDeclaredMethod(beanClass, methodName, parameterTypes);
		
		if(method == null) {
			StringBuilder sb = new StringBuilder();
			for (Class<?> oneParameterType : parameterTypes) {
				sb.append(oneParameterType.getName() + ", ");
			}
			
			String parameterTypesAsString = sb.length() > 2 ? sb.substring(0, sb.length() - 2) : sb.toString();
			
			throw new IllegalArgumentException(
				String.format("Type %s doesn't have a method %s(%s).", beanClass, methodName, parameterTypesAsString)
			);
		}
	}

	public MethodParameterConstraintMappingCreationalContext constraint(ConstraintDef<?, ?> definition) {
	
		mapping.addMethodConstraintConfig( ConfiguredConstraint.forParameter(
				definition, method, parameterIndex
		));
		return this;
	}

	/**
	 * Marks the currently selected method parameter as cascadable.
	 *
	 * @return Returns itself for method chaining.
	 */
	public MethodParameterConstraintMappingCreationalContext valid() {
		mapping.addMethodCascadeConfig(
				new MethodCascadeDef(
						beanClass, methodName, parameterTypes, parameterIndex, PARAMETER
				)
		);
		return this;
	}

	/**
	 * Changes the parameter for which added constraints apply.
	 *
	 * @param index The parameter index.
	 *
	 * @return Returns a new {@code ConstraintsForTypeMethodElement} instance allowing method chaining.
	 */
	public MethodParameterConstraintMappingCreationalContext parameter(int index) {
		return new MethodParameterConstraintMappingCreationalContextImpl(
				beanClass, methodName, parameterTypes, index, mapping
		);
	}

	/**
	 * Defines constraints on the return value of the current method.
	 *
	 * @return Returns a new {@code ConstraintsForTypeMethodElement} instance allowing method chaining.
	 */
	public MethodReturnValueConstraintMappingCreationalContext returnValue() {
		return new MethodReturnValueConstraintMappingCreationalContextImpl(
				beanClass, methodName, parameterTypes, mapping
		);
	}

}
