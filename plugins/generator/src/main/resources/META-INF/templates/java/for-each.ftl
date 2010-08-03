for (${modelItem.varType} ${modelItem.var}: ${modelItem.itemsExpression}) {
	<#list modelItem.statements as subStatement>
	${subStatement.code}
	</#list>
}