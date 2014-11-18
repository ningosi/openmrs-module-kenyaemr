<div class="ke-panel-frame">
	<div class="ke-panel-heading">Patient Summaries- Front Page</div>
	<div class="ke-panel-content" style="background-color: #F3F9FF">
		<table width="100%">
			<tr>
				<td>

					<table width="50%">
						<div class="ke-panel-frame">
							<div class="ke-panel-heading">Patient Profile</div>
							<tr>
								<td>Facility Name: ${ profile.facilityName }</td>
								<td>Patient Clinic Number: ${ profile.patientClinicNumber }</td>
							</tr>
							<tr>
								<td>Unique Patient Number:${ profile.uniquePatientNumber }</td>
								<td>Patient's Names: ${ profile.names }</td>
							</tr>
							<tr>
								<td>Sex ${ profile.gender }</td>
								<td>Date of Birth: ${ profile.dob } Age: ${ profile.age }</td>
							</tr>
							<tr>
								<td>Postal Address: ${ profile.address }</td>
								<td>Tel Contact: ${ profile.tel }</td>
							</tr>
						</div>
					</table>
				</td>
				<td valign="top">

					<table>
						<div class="ke-panel-frame">
							<div class="ke-panel-heading">ARV THERAPY</div>
							<tr>
								<td>Date Medically Eligible  ${ profile.tel }</td>
								<td></td>
								<td></td>
								<td></td>
							</tr>
							<tr>
								<td></td>
								<td></td>
								<td></td>
								<td></td>
							</tr>
						</div>
					</table>
				</td>
			</tr>
		</table>
	</div>
</div>