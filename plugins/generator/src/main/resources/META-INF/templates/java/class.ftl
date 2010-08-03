package ${modelItem.package.name};

<#list modelItem.imports as import>
import ${import.name};
</#list>

<@renderCommonJavaElementStuff element=modelItem />class ${modelItem.simpleName} <#if modelItem.superClass.name != 'java.lang.Object'>extends ${modelItem.superClass.simpleName} </#if>{
	<#list modelItem.fields as field>
	<@renderCommonJavaElementStuff element=field />${field.type.simpleName}${field.genericSignature} ${field.name}<#if field.value??> = ${field.value}</#if>;
	</#list>
	
	<#list modelItem.methods as method>
	<@renderCommonJavaElementStuff element=method />${method.returnType.simpleName} ${method.name}(<#list method.arguments as argument>${argument.type.simpleName} ${argument.name}<#if argument_has_next>, </#if></#list>) 
	<#if !method.exceptions.empty>
		throws <#list method.exceptions as exception>${exception.simpleName}<#if exception_has_next>, </#if></#list>
	</#if> {
		<#if method.methodBody??>
			<#list method.methodBody.statements as methodStatement>
			${methodStatement.code}
			</#list>
		</#if>
	}
	</#list>
}

<#macro renderCommonJavaElementStuff element>

<#if !element.comments.empty>
/*
<#list element.comments as comment>
 * ${comment.value}
</#list>
 */
</#if>
<#list element.annotations as annotation>
@${annotation.type.simpleName}<#if !annotation.arguments.empty>(<#list annotation.arguments as argument>${argument}<#if argument_has_next>, </#if></#list>)</#if>
</#list>
<#list element.modifiers as modifier>${modifier} </#list></#macro>