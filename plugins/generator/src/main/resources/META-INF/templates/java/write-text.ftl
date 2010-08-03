{
	Object text = ${modelItem.textExpression};
	if (text != null) {
		${responseWriterVariable}.writeText(text, null); 
	}
}