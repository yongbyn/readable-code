package cleancode.studycafe.tobe;

import java.util.List;
import java.util.Optional;

import cleancode.studycafe.tobe.exception.AppException;
import cleancode.studycafe.tobe.io.InputHandler;
import cleancode.studycafe.tobe.io.OutputHandler;
import cleancode.studycafe.tobe.io.StudyCafeFileHandler;
import cleancode.studycafe.tobe.model.StudyCafeLockerPass;
import cleancode.studycafe.tobe.model.StudyCafePass;
import cleancode.studycafe.tobe.model.StudyCafePassType;

public class StudyCafePassMachine {

	private final InputHandler inputHandler = new InputHandler();
	private final OutputHandler outputHandler = new OutputHandler();
	private final StudyCafeFileHandler studyCafeFileHandler = new StudyCafeFileHandler();

	public void run() {
		try {
			outputHandler.showWelcomeMessage();
			outputHandler.showAnnouncement();

			StudyCafePass selectedPass = selectStudyCafePass();
			StudyCafeLockerPass selectedLockerPass = selectLockerPass(selectedPass);

			outputHandler.showPassOrderSummary(selectedPass, selectedLockerPass);
		} catch (AppException e) {
			outputHandler.showSimpleMessage(e.getMessage());
		} catch (Exception e) {
			outputHandler.showSimpleMessage("알 수 없는 오류가 발생했습니다.");
		}
	}

	private StudyCafePass selectStudyCafePass() {
		StudyCafePassType studyCafePassType = selectPassType();
		List<StudyCafePass> studyCafePasses = findAllPassesBy(studyCafePassType);
		return selectPassFrom(studyCafePasses);
	}

	private StudyCafePassType selectPassType() {
		outputHandler.askPassTypeSelection();
		return inputHandler.getPassTypeSelectingUserAction();
	}

	private List<StudyCafePass> findAllPassesBy(StudyCafePassType studyCafePassType) {
		List<StudyCafePass> studyCafePasses = studyCafeFileHandler.readStudyCafePasses();
		return studyCafePasses.stream()
			.filter(studyCafePass -> studyCafePass.isSameType(studyCafePassType))
			.toList();
	}

	private StudyCafePass selectPassFrom(List<StudyCafePass> studyCafePasses) {
		outputHandler.showPassListForSelection(studyCafePasses);
		return inputHandler.getSelectPass(studyCafePasses);
	}

	private StudyCafeLockerPass selectLockerPass(StudyCafePass studyCafePass) {
		if (doesNotSupportLockerPass(studyCafePass)) {
			return null;
		}

		Optional<StudyCafeLockerPass> optionalLockerPass = findLockerPass(studyCafePass);

		if (optionalLockerPass.isPresent()) {
			StudyCafeLockerPass lockerPass = optionalLockerPass.get();
			boolean isLockerPassConfirmed = confirmLockerPassSelection(lockerPass);
			if (isLockerPassConfirmed) {
				return lockerPass;
			}
		}
		return null;
	}

	private boolean doesNotSupportLockerPass(StudyCafePass selectedPass) {
		return selectedPass.isSameType(StudyCafePassType.FIXED);
	}

	private Optional<StudyCafeLockerPass> findLockerPass(StudyCafePass studyCafePass) {
		List<StudyCafeLockerPass> lockerPasses = studyCafeFileHandler.readLockerPasses();
		return lockerPasses.stream()
			.filter(lockerPass -> isSameTypeAndDuration(lockerPass, studyCafePass))
			.findFirst();
	}

	private boolean isSameTypeAndDuration(StudyCafeLockerPass lockerPass, StudyCafePass studyCafePass) {
		return lockerPass.getPassType() == studyCafePass.getPassType()
			&& lockerPass.getDuration() == studyCafePass.getDuration();
	}

	private boolean confirmLockerPassSelection(StudyCafeLockerPass lockerPass) {
		outputHandler.askLockerPass(lockerPass);
		return inputHandler.getLockerSelection();
	}

}