<div class="ke-panel-frame">
	<div class="ke-panel-heading">Patient Summaries- Back page</div>
	<div class="ke-panel-content" style="background-color: #F3F9FF">
		<% if (!allEncounters) { %>
			No Visit summaries captured for this patient
		<%}%>
		<% if (allEncounters) { %>
			<% allEncounters.each { encounter -> %>
				<fieldset>
					<legend>Visit on -  ${ encounter.encounterDatetime } at ${ encounter.location.name }</legend>
					<table>
						<tr>
							<td>Visit Type</td>
							<td>${ visitDetails.visittype }</td>
						</tr>
						<tr>
							<td>Scheduled</td>
							<td>${ visitDetails.scheduled }</td>
						</tr>
					</table>
				</fieldset>
		<% } %>
		<%}%>
	</div>

</div>