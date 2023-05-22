package io.mosip.ivv.e2e.methods;

import java.io.FileOutputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Properties;
import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONObject;
import io.mosip.ivv.core.base.StepInterface;
import io.mosip.ivv.core.exceptions.RigInternalError;
import io.mosip.ivv.orchestrator.BaseTestCaseUtil;
import io.mosip.ivv.orchestrator.TestRunner;
import io.restassured.response.Response;

public class GetResidentData extends BaseTestCaseUtil implements StepInterface {
	Logger logger = Logger.getLogger(GetResidentData.class);

	@Override
	public void run() throws RigInternalError {
		// cleanData();
		int nofResident = 1;
		String ageCategory = null;
		Boolean bSkipGuardian = false;
//		Boolean isForChildPacket = false;
		HashMap<String, String> genderAndBioFlag = new HashMap<String, String>();
		String missFields = null;
		String[] bioFlag = null;
		if (!step.getParameters().isEmpty() && step.getParameters().size() > 3) {
			nofResident = Integer.parseInt(step.getParameters().get(0));
			ageCategory = step.getParameters().get(1);
			bSkipGuardian = Boolean.parseBoolean(step.getParameters().get(2));

			if (step.getParameters().size() >= 3) {

				if (step.getParameters().get(3).contains("@@")) {

					bioFlag = step.getParameters().get(3).split("@@");
					genderAndBioFlag.put("Gender", bioFlag[0]);
					genderAndBioFlag.put("Iris", bioFlag[1]);
					genderAndBioFlag.put("Finger", bioFlag[2]);
					genderAndBioFlag.put("Face", bioFlag[3]);
				}

				else {

					genderAndBioFlag.put("Gender", step.getParameters().get(3));
					genderAndBioFlag.put("Iris", "true");
					genderAndBioFlag.put("Finger", "true");
					genderAndBioFlag.put("Face", "true");

				}

			}

			if (step.getParameters().size() > 4)
				missFields = step.getParameters().get(4).replaceAll("@@", ",");
//			if (step.getParameters().size() > 5)
//				isForChildPacket = Boolean.parseBoolean(step.getParameters().get(5));

			if (ageCategory.equalsIgnoreCase("minor") || ageCategory.equalsIgnoreCase("infant")) { // used for child packet processing [1,true,false,Any,-1,true]
				step.getScenario()
						.setGeneratedResidentData(packetUtility.generateResidents(nofResident, ageCategory, bSkipGuardian,
								(missFields.equalsIgnoreCase("-1")) ? null : missFields, genderAndBioFlag, step));
			} else {
				cleanData();
				Response response = packetUtility.generateResident(nofResident, ageCategory, bSkipGuardian, missFields,
						genderAndBioFlag, step);
				JSONArray resp = new JSONObject(response.getBody().asString()).getJSONArray("response");
				// neeha List<String> residentPaths = new ArrayList<>();
				for (int i = 0; i < resp.length(); i++) {
					JSONObject obj = resp.getJSONObject(i);
					String resFilePath = obj.get("path").toString();
					String id = obj.get("id").toString();
					if (step.getOutVarName() != null)
						step.getScenario().getVariables().put(step.getOutVarName(), resFilePath);

					// neeha residentPaths.add(resFilePath);
					// TODO : REMOVE AFTER TESTING
					step.getScenario().getResidentTemplatePaths().put(resFilePath, null); // step.getScenario().getResidentTemplatePaths().put(resFilePath,
																							// null);
					// step.getScenario().getResidentTemplatePaths().put("C:\\Users\\ALOK~1.KUM\\AppData\\Local\\Temp\\residents_3868007285188428668\\3717670314.json",
					// null);

					step.getScenario().getResidentPersonaIdPro().put(id, resFilePath); // step.getScenario().getResidentPersonaIdPro().put(id,
																						// resFilePath);
					// step.getScenario().getResidentPersonaIdPro().put("3717670314",
					// "C:\\Users\\ALOK~1.KUM\\AppData\\Local\\Temp\\residents_3868007285188428668\\3717670314.json");
				}
				if (!step.getScenario().getResidentPersonaIdPro().isEmpty())
					storeProp(step.getScenario().getResidentPersonaIdPro());

			}
		} else {
			logger.warn("Input parameter missing [nofResident/bAdult/bSkipGuardian/gender]");
			throw new RigInternalError("Input parameter missing [nofResident/bAdult/bSkipGuardian/gender]");
		}
	}

	private static void storeProp(Properties prop) {
		String filePath = TestRunner.getExternalResourcePath() + props.getProperty("ivv.path.deviceinfo.folder")
				+ "step.getScenario().getResidentPersonaIdPro().properties";
		OutputStream output = null;
		try {
			output = new FileOutputStream(filePath);
			prop.store(output, null);
			output.close();
			output.flush();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void cleanData() {
		step.getScenario().getPridsAndRids().clear(); // step.getScenario().getPridsAndRids().clear();
		step.getScenario().getUinReqIds().clear(); // step.getScenario().getUinReqIds().clear();
		step.getScenario().getResidentTemplatePaths().clear();// step.getScenario().getResidentTemplatePaths().clear();
		step.getScenario().getResidentPathsPrid().clear(); // esidentPathsPrid.clear();
		step.getScenario().getTemplatePacketPath().clear();// step.getScenario().getTemplatePacketPath().clear();
	}
}
