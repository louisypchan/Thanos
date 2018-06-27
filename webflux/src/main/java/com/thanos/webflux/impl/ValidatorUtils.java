package com.thanos.webflux.impl;

import com.thanos.common.error.ErrorEnum;
import com.thanos.webflux.Util;
import org.springframework.boot.autoconfigure.validation.ValidatorAdapter;
import org.springframework.context.ApplicationContext;
import org.springframework.core.MethodParameter;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.validation.FieldError;
import org.springframework.validation.Validator;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.support.WebExchangeDataBinder;
import reactor.core.publisher.Mono;

import javax.xml.bind.ValidationException;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.List;

/****************************************************************************
 Copyright (c) 2017 Louis Y P Chen.
 Permission is hereby granted, free of charge, to any person obtaining a copy
 of this software and associated documentation files (the "Software"), to deal
 in the Software without restriction, including without limitation the rights
 to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 copies of the Software, and to permit persons to whom the Software is
 furnished to do so, subject to the following conditions:
 The above copyright notice and this permission notice shall be included in
 all copies or substantial portions of the Software.
 THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 THE SOFTWARE.
 ****************************************************************************/
public class ValidatorUtils {

    private Validator validator;

    private static ValidatorUtils instance = null;

    private ValidatorUtils(ApplicationContext ctx){
        this.validator = ValidatorAdapter.get(ctx, null);
    }

    public static ValidatorUtils get(ApplicationContext ctx){
        if(instance == null){
            instance = new ValidatorUtils(ctx);
        }
        return instance;
    }

    public <T,E> Mono<T> validate(Class<E> cls, String name, T target){
        Method method  = null;
        try {
            method = cls.getMethod(name, Object.class);
        } catch (NoSuchMethodException e) {
            return Util.error(ErrorEnum.NO_SUCH_METHOD.getKey(), e.getMessage());
        }
        MethodParameter parameter = new MethodParameter(method, 0);
        Annotation[] annotations = parameter.getParameterAnnotations();
        Object[] hints = null;
        for (Annotation ann : annotations) {
            Validated validatedAnn = AnnotationUtils.getAnnotation(ann, Validated.class);
            if (validatedAnn != null || ann.annotationType().getSimpleName().startsWith("Valid")) {
                Object result = (validatedAnn != null ? validatedAnn.value() : AnnotationUtils.getValue(ann));
                hints = (result instanceof Object[] ? (Object[]) result : new Object[] {result});
                break;
            }
        }
        WebExchangeDataBinder binder = new WebExchangeDataBinder(target);
        binder.setValidator(this.validator);
        binder.validate(hints);
        if(binder.getBindingResult().hasErrors()){
            List<FieldError> errors = binder.getBindingResult().getFieldErrors();
            StringBuffer sb = new StringBuffer();
            for(FieldError error : errors){
                sb.append(String.format("%s:%s;", error.getField(), error.getDefaultMessage()));
            }
            return Util.error(ErrorEnum.PARAMETERS_INVALID.getKey(), sb.toString());
        }
        return Mono.just(target);
    }
}
