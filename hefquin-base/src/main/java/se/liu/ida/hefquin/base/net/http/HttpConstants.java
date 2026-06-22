package se.liu.ida.hefquin.base.net.http;

public class HttpConstants
{
	// request headers
	public static final String X_HEADER_SKIP_EXECUTION = "X-HeFQUIN-Skip-Execution";
	public static final String X_HEADER_PRINT_SOURCE_ASSIGNMENT = "X-HeFQUIN-Print-Source-Assignment";
	public static final String X_HEADER_PRINT_LOGICAL_PLAN = "X-HeFQUIN-Print-Logical-Plan";
	public static final String X_HEADER_PRINT_PHYSICAL_PLAN = "X-HeFQUIN-Print-Physical-Plan";
	public static final String X_HEADER_PRINT_EXECUTABLE_PLAN = "X-HeFQUIN-Print-Executable-Plan";

	// response JSON fields
	public static final String JSON_RESULT = "result";
	public static final String JSON_SOURCE_ASSIGNMENT = "sourceAssignment";
	public static final String JSON_LOGICAL_PLAN = "logicalPlan";
	public static final String JSON_PHYSICAL_PLAN = "physicalPlan";
	public static final String JSON_EXECUTABLE_PLAN = "executablePlan";
	public static final String JSON_EXCEPTIONS = "exceptions";
}
